
package de.brummbeobachter.app

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class WaterfallResult(
    val bitmap: Bitmap,
    val sourceFileName: String,
    val minDb: Double,
    val maxDb: Double,
    val frameCount: Int,
    val durationSeconds: Double,
    val l50Db: Double,
    val l90Db: Double,
    val statsMinHz: Double,
    val statsMaxHz: Double,
    val comment: String?,
    val imageFileName: String
)

object WaterfallGenerator {

    const val FREQ_MIN_HZ = 0.0
    const val FREQ_MAX_HZ = 140.0

    // Adaptive Farbschalierung bleibt über 2–140 Hz wie in v0.4.54.
    const val LEVEL_STATS_MIN_HZ = 2.0
    const val LEVEL_STATS_MAX_HZ = 140.0

    const val PERCENTILE_LOW = 0.05
    const val PERCENTILE_HIGH = 0.995

    const val LEVEL_MARGIN_DB = 3.0
    const val MIN_LEVEL_SPAN_DB = 30.0

    // Numerical safety bounds only. They are deliberately much wider than the display range.
    // v0.5.19 clipped gain-referenced FFT bins at -120 dBFS before color mapping. With
    // +50 dB recording gain this collapsed many different bins onto the same black value and
    // could make the waterfall disappear although L50/L90 and the colorbar were visible.
    // Keep the physical/gain-referenced values here; clamp only when converting to a color.
    const val LEVEL_HARD_MIN_DB = -240.0
    const val LEVEL_HARD_MAX_DB = 20.0

    const val FFT_SIZE = 1024
    const val HOP_SIZE = 256

    private const val OUTPUT_WIDTH = 980
    private const val OUTPUT_HEIGHT = 1280

    private data class ParsedWav(
        val sampleRateHz: Int,
        val samples: DoubleArray,
        val pcm16: ShortArray
    )

    suspend fun generate(
        wavBytes: ByteArray,
        sourceFileName: String,
        comment: String?,
        gainDb: Int,
        startEpochMs: Long? = null,
        statsMinHz: Double = WaterfallStatsPolicy.DEFAULT_MIN_HZ,
        statsMaxHz: Double = WaterfallStatsPolicy.DEFAULT_MAX_HZ,
        levelAuto: Boolean = true,
        levelMinDbAt50: Int = WaterfallLevelPolicy.DEFAULT_MIN_DB_AT_50,
        levelMaxDbAt50: Int = WaterfallLevelPolicy.DEFAULT_MAX_DB_AT_50
    ): WaterfallResult =
        withContext(
            Dispatchers.Default
        ) {
            val wav =
                parseWavPcm16Mono(
                    wavBytes
                )

            require(
                wav.sampleRateHz > 0
            ) {
                "Ungültige Samplerate"
            }

            require(
                wav.samples.size >=
                    FFT_SIZE
            ) {
                "Audioaufnahme zu kurz"
            }

            // The stored observer WAV can contain the configured digital recording gain.
            // All displayed spectrum/spectrogram dB values are referred back to the pre-gain
            // input exactly once. Analyzer PRE-GAIN saves pass gainDb=0 here.
            val appliedGainDb = AudioRecordingPolicy.normalizeGainDb(gainDb).toDouble()

            val freqResolution =
                wav.sampleRateHz
                    .toDouble() /
                    FFT_SIZE.toDouble()

            val normalizedStatsMinHz =
                statsMinHz.coerceIn(FREQ_MIN_HZ, FREQ_MAX_HZ)
            val normalizedStatsMaxHz =
                statsMaxHz.coerceIn(FREQ_MIN_HZ, FREQ_MAX_HZ)

            require(normalizedStatsMinHz < normalizedStatsMaxHz) {
                "Ungültiger L50/L90-Frequenzbereich"
            }

            val firstDisplayBin =
                max(
                    0,
                    (FREQ_MIN_HZ /
                        freqResolution)
                        .roundToInt()
                )

            val lastDisplayBin =
                min(
                    FFT_SIZE / 2,
                    (FREQ_MAX_HZ /
                        freqResolution)
                        .roundToInt()
                )

            val firstLevelStatsBin =
                max(
                    firstDisplayBin,
                    (LEVEL_STATS_MIN_HZ / freqResolution).roundToInt()
                )

            val lastLevelStatsBin =
                min(
                    lastDisplayBin,
                    (LEVEL_STATS_MAX_HZ / freqResolution).roundToInt()
                )

            val firstL50L90Bin =
                max(
                    firstDisplayBin,
                    (normalizedStatsMinHz / freqResolution).roundToInt()
                )

            val lastL50L90Bin =
                min(
                    lastDisplayBin,
                    (normalizedStatsMaxHz / freqResolution).roundToInt()
                )

            val frames =
                1 +
                    (
                        wav.samples.size -
                            FFT_SIZE
                        ) /
                        HOP_SIZE

            val displayBins =
                lastDisplayBin -
                    firstDisplayBin + 1

            val spectrum =
                Array(
                    frames
                ) {
                    DoubleArray(
                        displayBins
                    )
                }

            val statsValues =
                ArrayList<Double>(
                    frames *
                        max(
                            1,
                            lastLevelStatsBin -
                                firstLevelStatsBin + 1
                        )
                )

            val broadbandFrameLevels =
                ArrayList<Double>(
                    frames
                )

            val window =
                DoubleArray(
                    FFT_SIZE
                ) {
                    n ->
                    // Hann window.
                    0.5 -
                        0.5 *
                        cos(
                            2.0 *
                                PI *
                                n.toDouble() /
                                (
                                    FFT_SIZE - 1
                                ).toDouble()
                        )
                }

            val coherentGain =
                window.average()

            for (
                frame in
                0 until frames
            ) {
                val start =
                    frame * HOP_SIZE

                val real =
                    DoubleArray(
                        FFT_SIZE
                    )

                val imag =
                    DoubleArray(
                        FFT_SIZE
                    )

                for (
                    n in
                    0 until FFT_SIZE
                ) {
                    real[n] =
                        wav.samples[
                            start + n
                        ] * window[n]
                }

                fftInPlace(
                    real,
                    imag
                )

                var framePower =
                    0.0

                for (
                    bin in
                    firstDisplayBin
                        ..lastDisplayBin
                ) {
                    val magnitude =
                        sqrt(
                            real[bin] *
                                real[bin] +
                                imag[bin] *
                                imag[bin]
                        )

                    val oneSidedScale =
                        if (
                            bin == 0 ||
                            bin ==
                                FFT_SIZE / 2
                        ) {
                            1.0
                        } else {
                            2.0
                        }

                    val amplitude =
                        oneSidedScale *
                            magnitude /
                            (
                                FFT_SIZE
                                    .toDouble() *
                                    coherentGain
                                )

                    val power =
                        amplitude *
                            amplitude

                    val db =
                        GainReferencePolicy.compensateDb(
                            20.0 * log10(max(amplitude, 1.0e-12)),
                            appliedGainDb
                        ).coerceIn(LEVEL_HARD_MIN_DB, LEVEL_HARD_MAX_DB)

                    spectrum[
                        frame
                    ][
                        bin -
                            firstDisplayBin
                    ] = db

                    if (bin in firstLevelStatsBin..lastLevelStatsBin) {
                        statsValues.add(db)
                    }

                    if (bin in firstL50L90Bin..lastL50L90Bin) {
                        framePower += power
                    }
                }

                val frameDb =
                    GainReferencePolicy.compensateDb(
                        10.0 * log10(max(framePower, 1.0e-24)),
                        appliedGainDb
                    ).coerceIn(LEVEL_HARD_MIN_DB, LEVEL_HARD_MAX_DB)

                broadbandFrameLevels
                    .add(
                        frameDb
                    )
            }

            val limits =
                if (levelAuto) {
                    adaptiveLevelRange(statsValues)
                } else {
                    WaterfallLevelPolicy.effectiveManualRange(
                        gainDb = gainDb,
                        minDbAt50 = levelMinDbAt50,
                        maxDbAt50 = levelMaxDbAt50
                    ).let { it.minDb to it.maxDb }
                }

            val sharedLevels =
                if (wav.sampleRateHz == StatisticsWindowEngine.RATE_HZ &&
                    wav.pcm16.size >= StatisticsWindowEngine.RATE_HZ &&
                    wav.pcm16.size % StatisticsWindowEngine.RATE_HZ == 0) {
                    StatisticsWindowEngine.analyzeOverall(
                        pcm = wav.pcm16,
                        gainDb = appliedGainDb,
                        minHz = normalizedStatsMinHz,
                        maxHz = normalizedStatsMaxHz
                    )
                } else null

            val l50Db =
                sharedLevels?.l50 ?: percentile(
                    broadbandFrameLevels
                        .sorted(),
                    0.50
                )

            val l90Db =
                sharedLevels?.l90 ?: percentile(
                    broadbandFrameLevels
                        .sorted(),
                    0.10
                )

            val imageFileName =
                buildImageFileName(
                    sourceFileName =
                        sourceFileName,
                    gainDb = gainDb,
                    l50Db = l50Db,
                    l90Db = l90Db
                )

            val bitmap =
                renderWaterfall(
                    spectrum = spectrum,
                    sampleRateHz =
                        wav.sampleRateHz,
                    minDb = limits.first,
                    maxDb = limits.second,
                    durationSeconds =
                        wav.samples.size
                            .toDouble() /
                            wav.sampleRateHz
                                .toDouble(),
                    startEpochMs = startEpochMs,
                    sourceFileName =
                        sourceFileName,
                    l50Db = l50Db,
                    l90Db = l90Db,
                    statsMinHz = normalizedStatsMinHz,
                    statsMaxHz = normalizedStatsMaxHz,
                    comment = comment,
                    levelAuto = levelAuto
                )

            WaterfallResult(
                bitmap = bitmap,
                sourceFileName =
                    sourceFileName,
                minDb = limits.first,
                maxDb = limits.second,
                frameCount = frames,
                durationSeconds =
                    wav.samples.size
                        .toDouble() /
                        wav.sampleRateHz
                            .toDouble(),
                l50Db = l50Db,
                l90Db = l90Db,
                statsMinHz = normalizedStatsMinHz,
                statsMaxHz = normalizedStatsMaxHz,
                comment = comment,
                imageFileName =
                    imageFileName
            )
        }

    fun savePng(
        context: Context,
        result: WaterfallResult
    ): String =
        saveImages(
            context = context,
            result = result,
            outputFormat = WaterfallOutputFormat.PNG
        ).first()

    fun saveImages(
        context: Context,
        result: WaterfallResult,
        outputFormat: WaterfallOutputFormat,
        overwriteExisting: Boolean = false
    ): List<String> {
        val existing = existingOutputNames(context, result, outputFormat)
        if (existing.isNotEmpty() && !overwriteExisting) {
            error("Bilddatei bereits vorhanden: ${existing.joinToString(", ")}")
        }
        if (overwriteExisting) {
            existing.forEach { deleteOutputName(context, it) }
        }

        return when (outputFormat) {
            WaterfallOutputFormat.PNG ->
                listOf(
                    saveBitmap(
                        context = context,
                        result = result,
                        extension = "png",
                        mimeType = "image/png",
                        compressFormat = Bitmap.CompressFormat.PNG,
                        quality = 100
                    )
                )

            WaterfallOutputFormat.JPG ->
                listOf(
                    saveBitmap(
                        context = context,
                        result = result,
                        extension = "jpg",
                        mimeType = "image/jpeg",
                        compressFormat = Bitmap.CompressFormat.JPEG,
                        quality = 95
                    )
                )

            WaterfallOutputFormat.PNG_AND_JPG ->
                listOf(
                    saveBitmap(
                        context = context,
                        result = result,
                        extension = "png",
                        mimeType = "image/png",
                        compressFormat = Bitmap.CompressFormat.PNG,
                        quality = 100
                    ),
                    saveBitmap(
                        context = context,
                        result = result,
                        extension = "jpg",
                        mimeType = "image/jpeg",
                        compressFormat = Bitmap.CompressFormat.JPEG,
                        quality = 95
                    )
                )
        }
    }

    fun outputNames(
        result: WaterfallResult,
        outputFormat: WaterfallOutputFormat
    ): List<String> {
        val baseName = result.imageFileName.substringBeforeLast(
            ".",
            result.imageFileName
        )
        return when (outputFormat) {
            WaterfallOutputFormat.PNG -> listOf("$baseName.png")
            WaterfallOutputFormat.JPG -> listOf("$baseName.jpg")
            WaterfallOutputFormat.PNG_AND_JPG -> listOf("$baseName.png", "$baseName.jpg")
        }
    }

    fun existingOutputNames(
        context: Context,
        result: WaterfallResult,
        outputFormat: WaterfallOutputFormat
    ): List<String> =
        outputNames(result, outputFormat).filter { outputNameExists(context, it) }

    private fun outputNameExists(
        context: Context,
        imageName: String
    ): Boolean {
        val appContext = context.applicationContext
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val projection = arrayOf(MediaStore.MediaColumns._ID)
            val selection =
                "${MediaStore.MediaColumns.DISPLAY_NAME}=? AND ${MediaStore.MediaColumns.RELATIVE_PATH}=?"
            val args = arrayOf(imageName, AudioArchive.PUBLIC_RELATIVE_PATH)
            appContext.contentResolver.query(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                args,
                null
            )?.use { it.moveToFirst() } ?: false
        } else {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "${PublicDataDirectory.folder}/$imageName"
            ).exists()
        }
    }

    private fun deleteOutputName(
        context: Context,
        imageName: String
    ) {
        val appContext = context.applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val selection =
                "${MediaStore.MediaColumns.DISPLAY_NAME}=? AND ${MediaStore.MediaColumns.RELATIVE_PATH}=?"
            val args = arrayOf(imageName, AudioArchive.PUBLIC_RELATIVE_PATH)
            appContext.contentResolver.delete(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                selection,
                args
            )
        } else {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "${PublicDataDirectory.folder}/$imageName"
            ).delete()
        }
    }

    private fun saveBitmap(
        context: Context,
        result: WaterfallResult,
        extension: String,
        mimeType: String,
        compressFormat: Bitmap.CompressFormat,
        quality: Int
    ): String {
        val appContext =
            context.applicationContext

        val baseName =
            result.imageFileName
                .substringBeforeLast(
                    ".",
                    result.imageFileName
                )

        val imageName =
            "$baseName.$extension"

        return if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.Q
        ) {
            val values =
                ContentValues().apply {
                    put(
                        MediaStore.MediaColumns.DISPLAY_NAME,
                        imageName
                    )
                    put(
                        MediaStore.MediaColumns.MIME_TYPE,
                        mimeType
                    )
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        AudioArchive.PUBLIC_RELATIVE_PATH
                    )
                    put(
                        MediaStore.MediaColumns.IS_PENDING,
                        1
                    )
                }

            val uri =
                appContext
                    .contentResolver
                    .insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        values
                    )
                    ?: error(
                        "Bild konnte in Downloads nicht angelegt werden"
                    )

            try {
                appContext
                    .contentResolver
                    .openOutputStream(uri)
                    ?.use {
                        output ->
                        check(
                            result.bitmap.compress(
                                compressFormat,
                                quality,
                                output
                            )
                        ) {
                            "Bildkompression fehlgeschlagen"
                        }
                    }
                    ?: error(
                        "Bild-Ausgabestrom konnte nicht geöffnet werden"
                    )

                val ready =
                    ContentValues().apply {
                        put(
                            MediaStore.MediaColumns.IS_PENDING,
                            0
                        )
                    }

                appContext
                    .contentResolver
                    .update(
                        uri,
                        ready,
                        null,
                        null
                    )

                "${AudioArchive.PUBLIC_DISPLAY_PATH}$imageName"
            } catch (
                error: Exception
            ) {
                runCatching {
                    appContext
                        .contentResolver
                        .delete(
                            uri,
                            null,
                            null
                        )
                }
                throw error
            }
        } else {
            val directory =
                File(
                    Environment
                        .getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS
                        ),
                    PublicDataDirectory.folder
                ).apply {
                    mkdirs()
                }

            val file =
                File(
                    directory,
                    imageName
                )

            file.outputStream()
                .use {
                    output ->
                    check(
                        result.bitmap.compress(
                            compressFormat,
                            quality,
                            output
                        )
                    ) {
                        "Bildkompression fehlgeschlagen"
                    }
                }

            file.absolutePath
        }
    }

    private fun adaptiveLevelRange(
        values: List<Double>
    ): Pair<Double, Double> {
        if (
            values.isEmpty()
        ) {
            return Pair(
                -90.0,
                -40.0
            )
        }

        val sorted =
            values.sorted()

        val low =
            percentile(
                sorted,
                PERCENTILE_LOW
            ) - LEVEL_MARGIN_DB

        val high =
            percentile(
                sorted,
                PERCENTILE_HIGH
            ) + LEVEL_MARGIN_DB

        var minDb =
            low.coerceIn(
                LEVEL_HARD_MIN_DB,
                LEVEL_HARD_MAX_DB
            )

        var maxDb =
            high.coerceIn(
                LEVEL_HARD_MIN_DB,
                LEVEL_HARD_MAX_DB
            )

        if (
            maxDb - minDb <
            MIN_LEVEL_SPAN_DB
        ) {
            val center =
                (
                    minDb + maxDb
                    ) / 2.0

            minDb =
                center -
                    MIN_LEVEL_SPAN_DB /
                    2.0

            maxDb =
                center +
                    MIN_LEVEL_SPAN_DB /
                    2.0

            if (
                minDb <
                LEVEL_HARD_MIN_DB
            ) {
                val shift =
                    LEVEL_HARD_MIN_DB -
                        minDb
                minDb += shift
                maxDb += shift
            }

            if (
                maxDb >
                LEVEL_HARD_MAX_DB
            ) {
                val shift =
                    maxDb -
                        LEVEL_HARD_MAX_DB
                minDb -= shift
                maxDb -= shift
            }

            minDb =
                minDb.coerceAtLeast(
                    LEVEL_HARD_MIN_DB
                )
            maxDb =
                maxDb.coerceAtMost(
                    LEVEL_HARD_MAX_DB
                )
        }

        if (
            maxDb <= minDb
        ) {
            maxDb =
                min(
                    LEVEL_HARD_MAX_DB,
                    minDb +
                        MIN_LEVEL_SPAN_DB
                )
        }

        return Pair(
            minDb,
            maxDb
        )
    }

    private fun percentile(
        sorted: List<Double>,
        fraction: Double
    ): Double {
        if (
            sorted.isEmpty()
        ) {
            return LEVEL_HARD_MIN_DB
        }

        if (
            sorted.size == 1
        ) {
            return sorted[0]
        }

        val position =
            fraction.coerceIn(
                0.0,
                1.0
            ) *
                (
                    sorted.size - 1
                    ).toDouble()

        val lower =
            position.toInt()
                .coerceIn(
                    0,
                    sorted.lastIndex
                )

        val upper =
            min(
                sorted.lastIndex,
                lower + 1
            )

        val mix =
            position -
                lower.toDouble()

        return sorted[lower] *
            (
                1.0 -
                    mix
                ) +
            sorted[upper] *
            mix
    }

    private fun renderWaterfall(
        spectrum: Array<DoubleArray>,
        sampleRateHz: Int,
        minDb: Double,
        maxDb: Double,
        durationSeconds: Double,
        startEpochMs: Long?,
        sourceFileName: String,
        l50Db: Double,
        l90Db: Double,
        statsMinHz: Double,
        statsMaxHz: Double,
        comment: String?,
        levelAuto: Boolean
    ): Bitmap {
        val bitmap =
            Bitmap.createBitmap(
                OUTPUT_WIDTH,
                OUTPUT_HEIGHT,
                Bitmap.Config.ARGB_8888
            )

        val canvas =
            Canvas(
                bitmap
            )

        val paint =
            Paint(
                Paint.ANTI_ALIAS_FLAG
            )

        canvas.drawColor(
            android.graphics.Color.BLACK
        )

        val left = 90
        val top = 140
        val right = 780
        val bottom = 1120

        val heatWidth =
            right - left
        val heatHeight =
            bottom - top

        val bins =
            spectrum.firstOrNull()
                ?.size ?: 0

        if (
            bins > 0 &&
            spectrum.isNotEmpty()
        ) {
            val heat =
                Bitmap.createBitmap(
                    heatWidth,
                    heatHeight,
                    Bitmap.Config
                        .ARGB_8888
                )

            for (
                y in
                0 until heatHeight
            ) {
                // time increases downward
                val frame =
                    (
                        y.toDouble() /
                            max(
                                1,
                                heatHeight - 1
                            ).toDouble() *
                            (
                                spectrum.size - 1
                                ).toDouble()
                        )
                        .roundToInt()
                        .coerceIn(
                            0,
                            spectrum.lastIndex
                        )

                for (
                    x in
                    0 until heatWidth
                ) {
                    val bin =
                        (
                            x.toDouble() /
                                max(
                                    1,
                                    heatWidth - 1
                                ).toDouble() *
                                (
                                    bins - 1
                                    ).toDouble()
                            )
                            .roundToInt()
                            .coerceIn(
                                0,
                                bins - 1
                            )

                    val value =
                        spectrum[
                            frame
                        ][bin]

                    val normalized =
                        (
                            (
                                value - minDb
                                ) /
                                max(
                                    1.0e-9,
                                    maxDb - minDb
                                )
                            )
                            .coerceIn(
                                0.0,
                                1.0
                            )

                    heat.setPixel(
                        x,
                        y,
                        infernoColor(
                            normalized
                        )
                    )
                }
            }

            canvas.drawBitmap(
                heat,
                left.toFloat(),
                top.toFloat(),
                paint
            )
        }

        paint.color =
            android.graphics.Color.WHITE
        paint.textSize =
            34f
        paint.typeface =
            Typeface.DEFAULT_BOLD
        canvas.drawText(
            "Wasserfallspektrum 0–140 Hz",
            36f,
            44f,
            paint
        )

        paint.textSize = 21f
        paint.typeface =
            Typeface.DEFAULT

        val sourceLine =
            sourceFileName.take(78)
        canvas.drawText(
            sourceLine,
            36f,
            74f,
            paint
        )

        canvas.drawText(
            "Pegel: %.1f … %.1f dBFS · %s".format(
                minDb,
                maxDb,
                if (levelAuto) "automatisch" else "manuell"
            ),
            36f,
            100f,
            paint
        )

        canvas.drawText(
            "L50/L90 %.0f–%.0f Hz: %.1f / %.1f dBFS".format(
                statsMinHz,
                statsMaxHz,
                l50Db,
                l90Db
            ),
            36f,
            126f,
            paint
        )

        val axisPaint =
            Paint(
                Paint.ANTI_ALIAS_FLAG
            ).apply {
                color =
                    android.graphics.Color.WHITE
                textSize = 20f
            }

        for (
            hz in
            listOf(
                0,
                20,
                40,
                60,
                80,
                100,
                120,
                140
            )
        ) {
            val x =
                left +
                    (
                        hz / 140.0 *
                            heatWidth
                        ).toInt()

            canvas.drawText(
                "$hz",
                x.toFloat() - 10f,
                1150f,
                axisPaint
            )
        }

        canvas.drawText(
            "Frequenz [Hz]",
            330f,
            1180f,
            axisPaint
        )

        val timeLabels =
            startEpochMs?.let {
                WaterfallTimeAxisPolicy.labels(
                    startEpochMs = it,
                    durationSeconds = durationSeconds
                )
            } ?: List(WaterfallTimeAxisPolicy.LABEL_COUNT) { "--:--:--" }

        timeLabels.forEachIndexed { index, label ->
            val fraction = index.toDouble() / (timeLabels.size - 1).toDouble()
            val y = top + (fraction * heatHeight).toInt()
            canvas.drawText(
                label,
                2f,
                y.toFloat() + 7f,
                axisPaint
            )
        }

        val colorBarLeft = 815
        val colorBarRight = 850

        for (
            y in
            top until bottom
        ) {
            val fraction =
                1.0 -
                    (
                        y - top
                        ).toDouble() /
                    max(
                        1,
                        heatHeight - 1
                    ).toDouble()

            paint.color =
                infernoColor(
                    fraction
                )

            canvas.drawRect(
                colorBarLeft.toFloat(),
                y.toFloat(),
                colorBarRight.toFloat(),
                (
                    y + 1
                    ).toFloat(),
                paint
            )
        }

        axisPaint.textSize =
            18f

        canvas.drawText(
            "%.0f".format(
                maxDb
            ),
            860f,
            (
                top + 6
                ).toFloat(),
            axisPaint
        )
        canvas.drawText(
            "%.0f".format(
                minDb
            ),
            860f,
            bottom.toFloat(),
            axisPaint
        )
        canvas.drawText(
            "dBFS",
            860f,
            (
                top +
                    heatHeight / 2
                ).toFloat(),
            axisPaint
        )

        if (
            !comment.isNullOrBlank()
        ) {
            val commentPaint =
                Paint(
                    Paint.ANTI_ALIAS_FLAG
                ).apply {
                    color =
                        android.graphics.Color.WHITE
                    textSize = 18f
                }

            val wrapped =
                wrapText(
                    "Kommentar: ${comment.trim()}",
                    84
                )

            var y =
                1205f
            for (
                line in
                wrapped.take(3)
            ) {
                canvas.drawText(
                    line,
                    36f,
                    y,
                    commentPaint
                )
                y += 22f
            }
        }

        return bitmap
    }

    fun infernoColor(
        value: Double
    ): Int {
        val t =
            value.coerceIn(
                0.0,
                1.0
            )

        val stops =
            arrayOf(
                doubleArrayOf(0.00, 0.001, 0.000, 0.014),
                doubleArrayOf(0.13, 0.129, 0.047, 0.290),
                doubleArrayOf(0.25, 0.341, 0.063, 0.429),
                doubleArrayOf(0.38, 0.558, 0.139, 0.402),
                doubleArrayOf(0.50, 0.735, 0.216, 0.330),
                doubleArrayOf(0.63, 0.865, 0.317, 0.226),
                doubleArrayOf(0.75, 0.953, 0.470, 0.106),
                doubleArrayOf(0.88, 0.988, 0.709, 0.190),
                doubleArrayOf(1.00, 0.988, 0.998, 0.645)
            )

        var lower =
            stops[0]
        var upper =
            stops.last()

        for (
            index in
            0 until
                stops.lastIndex
        ) {
            if (
                t >= stops[index][0] &&
                t <= stops[index + 1][0]
            ) {
                lower = stops[index]
                upper = stops[index + 1]
                break
            }
        }

        val range =
            max(
                1.0e-9,
                upper[0] - lower[0]
            )

        val mix =
            (
                t - lower[0]
                ) / range

        fun channel(
            index: Int
        ): Int =
            (
                (
                    lower[index] *
                        (1.0 - mix) +
                        upper[index] *
                        mix
                    ) * 255.0
                )
                .roundToInt()
                .coerceIn(
                    0,
                    255
                )

        return android.graphics.Color.rgb(
            channel(1),
            channel(2),
            channel(3)
        )
    }

    private fun buildImageFileName(
        sourceFileName: String,
        gainDb: Int,
        l50Db: Double,
        l90Db: Double
    ): String {
        val baseName =
            sourceFileName.substringBeforeLast(
                ".",
                sourceFileName
            )

        val maxFrequencyTag =
            FREQ_MAX_HZ.roundToInt()

        return buildString {
            append(
                baseName
            )
            append("_L50")
            append(
                encodeDb(
                    l50Db
                )
            )
            append("_L90")
            append(
                encodeDb(
                    l90Db
                )
            )
            append("_WF${maxFrequencyTag}.png")
        }
    }

    private fun encodeDb(
        value: Double
    ): String {
        val prefix =
            if (
                value < 0.0
            ) {
                "m"
            } else {
                "p"
            }

        val absValue =
            kotlin.math.abs(
                value
            )

        val text =
            "%.1f".format(
                absValue
            ).replace(
                ".",
                "p"
            )

        return prefix + text
    }

    private fun wrapText(
        text: String,
        maxChars: Int
    ): List<String> {
        if (
            text.length <= maxChars
        ) {
            return listOf(text)
        }

        val words =
            text.split(
                Regex("\\s+")
            )

        val lines =
            mutableListOf<String>()

        var current = ""

        for (
            word in
            words
        ) {
            val candidate =
                if (
                    current.isBlank()
                ) {
                    word
                } else {
                    "$current $word"
                }

            if (
                candidate.length <=
                maxChars
            ) {
                current = candidate
            } else {
                if (
                    current.isNotBlank()
                ) {
                    lines.add(current)
                }
                current = word
            }
        }

        if (
            current.isNotBlank()
        ) {
            lines.add(current)
        }

        return lines
    }

    private fun parseWavPcm16Mono(
        bytes: ByteArray
    ): ParsedWav {
        require(
            bytes.size >= 44
        ) {
            "WAV-Datei ist zu klein"
        }

        fun ascii(
            offset: Int,
            length: Int
        ): String =
            bytes.copyOfRange(
                offset,
                offset + length
            ).toString(
                Charsets.US_ASCII
            )

        require(
            ascii(0, 4) == "RIFF" &&
                ascii(8, 4) == "WAVE"
        ) {
            "Ungültiger WAV-Header"
        }

        var offset = 12
        var sampleRate = 0
        var channels = 0
        var bitsPerSample = 0
        var audioFormat = 0
        var dataOffset = -1
        var dataSize = 0

        while (
            offset + 8 <= bytes.size
        ) {
            val chunkId =
                ascii(
                    offset,
                    4
                )

            val chunkSize =
                ByteBuffer
                    .wrap(
                        bytes,
                        offset + 4,
                        4
                    )
                    .order(
                        ByteOrder
                            .LITTLE_ENDIAN
                    )
                    .int

            val contentOffset =
                offset + 8

            if (
                chunkSize < 0 ||
                contentOffset + chunkSize >
                bytes.size
            ) {
                break
            }

            when (chunkId) {
                "fmt " -> {
                    require(
                        chunkSize >= 16
                    ) {
                        "Ungültiger fmt-Chunk"
                    }

                    val fmt =
                        ByteBuffer
                            .wrap(
                                bytes,
                                contentOffset,
                                chunkSize
                            )
                            .order(
                                ByteOrder
                                    .LITTLE_ENDIAN
                            )

                    audioFormat =
                        fmt.short
                            .toInt() and
                            0xffff

                    channels =
                        fmt.short
                            .toInt() and
                            0xffff

                    sampleRate =
                        fmt.int

                    fmt.int
                    fmt.short

                    bitsPerSample =
                        fmt.short
                            .toInt() and
                            0xffff
                }

                "data" -> {
                    dataOffset =
                        contentOffset
                    dataSize =
                        chunkSize
                }
            }

            offset =
                contentOffset +
                    chunkSize +
                    (
                        chunkSize and 1
                        )
        }

        require(
            audioFormat == 1
        ) {
            "Nur PCM-WAV wird unterstützt"
        }
        require(
            channels == 1
        ) {
            "Nur Mono-WAV wird unterstützt"
        }
        require(
            bitsPerSample == 16
        ) {
            "Nur 16-Bit-WAV wird unterstützt"
        }
        require(
            sampleRate > 0 &&
                dataOffset >= 0 &&
                dataSize > 0
        ) {
            "WAV-Daten fehlen"
        }

        val sampleCount =
            dataSize / 2

        val samples =
            DoubleArray(
                sampleCount
            )
        val pcm16 = ShortArray(sampleCount)

        val buffer =
            ByteBuffer
                .wrap(
                    bytes,
                    dataOffset,
                    dataSize
                )
                .order(
                    ByteOrder
                        .LITTLE_ENDIAN
                )

        for (
            index in
            0 until sampleCount
        ) {
            val value = buffer.short
            pcm16[index] = value
            samples[index] =
                value.toDouble() /
                    32768.0
        }

        return ParsedWav(
            sampleRateHz =
                sampleRate,
            samples = samples,
            pcm16 = pcm16
        )
    }

    private fun fftInPlace(
        real: DoubleArray,
        imag: DoubleArray
    ) {
        val n =
            real.size

        require(
            n > 0 &&
                (
                    n and
                        (
                            n - 1
                            )
                    ) == 0
        ) {
            "FFT-Größe muss Zweierpotenz sein"
        }

        var j = 0
        for (
            i in 1 until n
        ) {
            var bit =
                n shr 1

            while (
                j and bit != 0
            ) {
                j =
                    j xor bit
                bit =
                    bit shr 1
            }

            j =
                j xor bit

            if (i < j) {
                val tempReal =
                    real[i]
                real[i] =
                    real[j]
                real[j] =
                    tempReal

                val tempImag =
                    imag[i]
                imag[i] =
                    imag[j]
                imag[j] =
                    tempImag
            }
        }

        var length = 2
        while (
            length <= n
        ) {
            val angle =
                -2.0 *
                    PI /
                    length.toDouble()

            val wLengthReal =
                cos(angle)
            val wLengthImag =
                sin(angle)

            var start = 0
            while (
                start < n
            ) {
                var wReal = 1.0
                var wImag = 0.0

                val half =
                    length / 2

                for (
                    k in 0 until half
                ) {
                    val even =
                        start + k
                    val odd =
                        even + half

                    val oddReal =
                        real[odd] *
                            wReal -
                            imag[odd] *
                            wImag

                    val oddImag =
                        real[odd] *
                            wImag +
                            imag[odd] *
                            wReal

                    real[odd] =
                        real[even] -
                            oddReal
                    imag[odd] =
                        imag[even] -
                            oddImag

                    real[even] +=
                        oddReal
                    imag[even] +=
                        oddImag

                    val nextWReal =
                        wReal *
                            wLengthReal -
                            wImag *
                            wLengthImag

                    wImag =
                        wReal *
                            wLengthImag +
                            wImag *
                            wLengthReal
                    wReal =
                        nextWReal
                }

                start += length
            }

            length =
                length shl 1
        }
    }
}
