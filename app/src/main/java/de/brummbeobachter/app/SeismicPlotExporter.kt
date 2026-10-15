package de.brummbeobachter.app

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.time.LocalDateTime
import kotlin.math.max

object SeismicPlotExporter {
    val PUBLIC_RELATIVE_PATH = PublicDataDirectory.relativePath
    val PUBLIC_DISPLAY_PATH = PublicDataDirectory.relativePath

    fun save(context: Context, snapshot: SeismicLiveService.Snapshot, nearPlotBitmap: Bitmap? = null): String {
        val fileName = SeismicPlotUiPolicy.exportFileName(
            snapshot.selectedDate,
            snapshot.station.network,
            snapshot.station.station,
            snapshot.channel,
            snapshot.sourceKind,
            LocalDateTime.now()
        )

        if (snapshot.sourceKind == "HLNUG_WBBU_IMAGE" && snapshot.sourceImageBytes != null) {
            return saveBitmap(context.applicationContext, renderWbbuPlot(snapshot.sourceImageBytes), fileName)
        }

        val bitmap = nearPlotBitmap ?: renderNearPlot(snapshot)
        return saveBitmap(context.applicationContext, bitmap, fileName)
    }

    fun renderNearPlot(snapshot: SeismicLiveService.Snapshot): Bitmap {
        val finalWidth = SeismicPlotUiPolicy.EXPORT_WIDTH_PX
        val finalHeight = SeismicPlotUiPolicy.EXPORT_HEIGHT_PX
        val supersample = SeismicPlotUiPolicy.EXPORT_SUPERSAMPLE
        val hiRes = Bitmap.createBitmap(
            SeismicPlotUiPolicy.MASTER_WIDTH_PX,
            SeismicPlotUiPolicy.MASTER_HEIGHT_PX,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(hiRes)
        canvas.scale(supersample.toFloat(), supersample.toFloat())
        canvas.drawColor(Color.WHITE)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 17f
        }
        val smallText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 12f
        }
        val axisText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 11f
        }
        val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(220, 220, 220)
            strokeWidth = 0.7f
        }
        val separatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.GRAY
            strokeWidth = 1f
        }
        val bluePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(25, 65, 220)
            strokeWidth = SeismicPlotUiPolicy.TRACE_STROKE_WIDTH_PX
        }
        val redPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(225, 35, 45)
            strokeWidth = SeismicPlotUiPolicy.TRACE_STROKE_WIDTH_PX
        }

        // WBBU-nahe Kopfzeile: Station links, Datum rechts, darunter kompakte Metadaten.
        val title = "${snapshot.station.network}.${snapshot.station.station}.${snapshot.channel}"
        canvas.drawText(title, 48f, 24f, titlePaint)
        val dateText = snapshot.selectedDate.toString()
        canvas.drawText(dateText, finalWidth - 48f - titlePaint.measureText(dateText), 24f, titlePaint)
        val meta = if (snapshot.amplitudeUnit == "nm/s") {
            "30 min/Zeile · sensitivitätsbasiert · Messwerte ×${snapshot.plotValueGain} · 40000 nm/s Referenz"
        } else {
            "30 min/Zeile · Counts-Skalierung · Messwerte ×${snapshot.plotValueGain}"
        }
        canvas.drawText(meta, 48f, 43f, smallText)
        canvas.drawLine(0f, 54f, finalWidth.toFloat(), 54f, separatorPaint)

        val topMargin = 64f
        val bottomAxisHeight = 38f
        val rows = snapshot.helicorderRows.size.coerceAtLeast(1)
        val plotBottom = finalHeight - bottomAxisHeight
        val rowHeight = (plotBottom - topMargin) / rows.toFloat()

        // Kompakter linker Rand: Maßstrecke -> Uhrzeit -> Plot.
        val legendWidth = 62f
        val timeLabelWidth = 56f
        val plotLeft = legendWidth + timeLabelWidth
        val plotRight = finalWidth - 26f
        val plotWidth = plotRight - plotLeft
        val fixedNmPerSecPerRow = SeismicAmplitudePolicy.WBBU_NM_PER_SEC_PER_ROW_SPACING.toFloat()

        snapshot.helicorderRows.forEachIndexed { rowIndex, row ->
            val centerY = topMargin + (rowIndex + 0.5f) * rowHeight
            canvas.drawLine(plotLeft, centerY, plotRight, centerY, gridPaint)
            val hh = row.startMinuteOfDay / 60
            val mm = row.startMinuteOfDay % 60
            val time = "%02d:%02d".format(hh, mm)
            canvas.drawText(time, legendWidth + timeLabelWidth - 5f - axisText.measureText(time), centerY + 4f, axisText)

            if (row.bins.isNotEmpty() && snapshot.helicorderMaxAbs > 0.0) {
                val paint = if (row.usePrimaryColor) bluePaint else redPaint
                val scaleY = if (snapshot.amplitudeUnit == "nm/s") {
                    rowHeight / fixedNmPerSecPerRow
                } else {
                    rowHeight * 0.46f / snapshot.helicorderMaxAbs.toFloat()
                }
                val traceValues = SeismicScheliPolicy.orderedTraceValues(row.bins)
                if (traceValues.isNotEmpty()) {
                    val path = Path()
                    val denom = max(1, traceValues.size - 1).toFloat()
                    traceValues.forEachIndexed { index, value ->
                        val x = plotLeft + plotWidth * index / denom
                        val y = centerY - value * scaleY
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = SeismicPlotUiPolicy.TRACE_STROKE_WIDTH_PX
                    paint.strokeJoin = Paint.Join.ROUND
                    paint.strokeCap = Paint.Cap.ROUND
                    canvas.drawPath(path, paint)
                }
            }
        }

        // Maßstrecke ganz links; Beschriftung direkt an der Strecke, inklusive Einheit.
        val legendPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            strokeWidth = 2.2f
        }
        val legendTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.DKGRAY
            textSize = 11f
        }
        val legendX = 42f
        val legendCenterY = topMargin + (plotBottom - topMargin) * 0.78f
        val legendHeight = if (snapshot.amplitudeUnit == "nm/s") {
            rowHeight * SeismicAmplitudePolicy.WBBU_REFERENCE_ROW_SPACINGS.toFloat()
        } else {
            rowHeight * 0.46f
        }
        val legendTop = (legendCenterY - legendHeight / 2f).coerceAtLeast(topMargin)
        val legendBottom = (legendCenterY + legendHeight / 2f).coerceAtMost(plotBottom)
        canvas.drawLine(legendX, legendTop, legendX, legendBottom, legendPaint)
        canvas.drawLine(legendX - 7f, legendTop, legendX + 7f, legendTop, legendPaint)
        canvas.drawLine(legendX - 7f, legendBottom, legendX + 7f, legendBottom, legendPaint)
        val legendLabel = if (snapshot.amplitudeUnit == "nm/s") {
            "40000 nm/s"
        } else {
            "${"%.0f".format(snapshot.helicorderMaxAbs)} counts"
        }
        canvas.save()
        val pivotX = legendX - 12f
        val pivotY = (legendTop + legendBottom) / 2f
        canvas.rotate(-90f, pivotX, pivotY)
        val labelWidth = legendTextPaint.measureText(legendLabel)
        canvas.drawText(legendLabel, pivotX - labelWidth / 2f, pivotY - 3f, legendTextPaint)
        canvas.restore()

        // WBBU-nahe X-Achse: 0, 5min ... 30min.
        for (minute in 0..30 step 5) {
            val x = plotLeft + plotWidth * minute / 30f
            canvas.drawLine(x, plotBottom, x, plotBottom + 7f, separatorPaint)
            val label = if (minute == 0) "0" else "${minute}min"
            val labelWidth = axisText.measureText(label)
            val tx = when (minute) {
                0 -> x
                30 -> x - labelWidth
                else -> x - labelWidth / 2f
            }
            canvas.drawText(label, tx, finalHeight - 12f, axisText)
        }
        canvas.drawLine(plotLeft, plotBottom, plotRight, plotBottom, separatorPaint)

        return rotateLeft(hiRes)
    }

    fun renderWbbuPlot(pngBytes: ByteArray): Bitmap {
        require(pngBytes.isNotEmpty()) { "WBBU-PNG ist leer" }
        val decoded = BitmapFactory.decodeByteArray(pngBytes, 0, pngBytes.size)
            ?: error("WBBU-PNG konnte nicht dekodiert werden")
        return rotateLeft(decoded)
    }

    fun rotateLeft(source: Bitmap): Bitmap {
        val matrix = Matrix().apply { postRotate(-90f) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }


    private fun savePngBytes(context: Context, pngBytes: ByteArray, fileName: String): String {
        require(pngBytes.isNotEmpty()) { "WBBU-PNG ist leer" }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, PUBLIC_RELATIVE_PATH)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: error("PNG konnte in Downloads nicht angelegt werden")
            try {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    output.write(pngBytes)
                    output.flush()
                } ?: error("PNG-Ausgabestrom konnte nicht geöffnet werden")
                context.contentResolver.update(uri, ContentValues().apply {
                    put(MediaStore.MediaColumns.IS_PENDING, 0)
                }, null, null)
                "$PUBLIC_DISPLAY_PATH$fileName"
            } catch (error: Exception) {
                runCatching { context.contentResolver.delete(uri, null, null) }
                throw error
            }
        } else {
            val directory = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                PublicDataDirectory.folder
            )
            check(directory.exists() || directory.mkdirs()) { "Zielverzeichnis konnte nicht angelegt werden" }
            val target = File(directory, fileName)
            target.writeBytes(pngBytes)
            "$PUBLIC_DISPLAY_PATH$fileName"
        }
    }

    private fun saveBitmap(context: Context, bitmap: Bitmap, fileName: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, PUBLIC_RELATIVE_PATH)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: error("PNG konnte in Downloads nicht angelegt werden")
            try {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                        "PNG-Kompression fehlgeschlagen"
                    }
                } ?: error("PNG-Ausgabestrom konnte nicht geöffnet werden")
                context.contentResolver.update(uri, ContentValues().apply {
                    put(MediaStore.MediaColumns.IS_PENDING, 0)
                }, null, null)
                "$PUBLIC_DISPLAY_PATH$fileName"
            } catch (error: Exception) {
                runCatching { context.contentResolver.delete(uri, null, null) }
                throw error
            }
        } else {
            val directory = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                PublicDataDirectory.folder
            ).apply { mkdirs() }
            val file = File(directory, fileName)
            file.outputStream().use { output ->
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                    "PNG-Kompression fehlgeschlagen"
                }
            }
            file.absolutePath
        }
    }
}
