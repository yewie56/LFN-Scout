package de.brummbeobachter.app

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import org.json.JSONObject
import org.json.JSONArray
import java.util.UUID

/** Records actual post-dynamics, post-fade PCM, never the raw rolling buffer.
 * PCM is written on a separate worker so AudioTrack playback is not delayed by file I/O.
 */
internal object AnalyzerMonitorRecording {
    private const val RATE = 48000
    private val mutex = Any()
    @Volatile private var active = false
    private var outputFile: File? = null
    private var writer: FileOutputStream? = null
    private var thread: Thread? = null
    private val blocks = java.util.concurrent.LinkedBlockingQueue<ByteArray>(512)
    private var samples: Long = 0
    private var writeAccounting = MonPcmWriteAccounting()
    private var failed = false
    private var startTime = OffsetDateTime.now()
    private data class Params(val center: Double, val width: Double, val gain: Double)
    private var firstParams = Params(20.0,12.0,-24.0)
    private val changes = mutableListOf<Pair<Long,Params>>()
    private fun currentParams() = Params(AnalyzerMonitor.centerHz,AnalyzerMonitor.bandwidthHz,AnalyzerMonitor.levelDb)
    fun noteParameters() = synchronized(mutex) {
        if (!active) return@synchronized
        val current = currentParams()
        if (changes.lastOrNull()?.second != current) changes.add((System.currentTimeMillis()) to current)
    }
    private fun number(value:Double):String =
        java.math.BigDecimal.valueOf(value).stripTrailingZeros().toPlainString()
    private fun safe(value:Double) = (if (value >= 0.0) "+" else "") + kotlin.math.round(value).toLong().toString()
    private fun rounded(value:Double):Double = java.math.BigDecimal.valueOf(value).setScale(2,java.math.RoundingMode.HALF_UP).toDouble()


    fun isActive() = active

    fun start(context: Context): Boolean = AnalyzerSetupTransitionCoordinator.withMonStart { synchronized(mutex) {
        if (active || outputFile != null) return@synchronized false
        try {
            val f = File(context.cacheDir, "MON_${System.currentTimeMillis()}_${UUID.randomUUID()}.wav")
            val stream = FileOutputStream(f)
            stream.write(ByteArray(44))
            outputFile = f
            writer = stream
            samples = 0L
            writeAccounting = MonPcmWriteAccounting()
            failed = false
            startTime=OffsetDateTime.now()
            firstParams=currentParams()
            changes.clear()
            changes.add(System.currentTimeMillis() to firstParams)
            blocks.clear()
            active = true
            thread = Thread({
                while (active || blocks.isNotEmpty()) {
                    val bytes = blocks.poll(150, java.util.concurrent.TimeUnit.MILLISECONDS) ?: continue
                    try { stream.write(bytes); writeAccounting.written(bytes.size); samples += bytes.size / 2L }
                    catch (_: Exception) { failed = true; active = false }
                }
                try { stream.flush();stream.close() } catch (_: Exception) { failed = true }
            }, "LFN-MON-WAV").also { it.start() }
            true
        } catch (_: Exception) {
            active=false; try {writer?.close()} catch (_: Exception) { };false
        }
    } }

    fun accept(buffer: ShortArray) {
        if (!active) return
        val bytes=ByteArray(buffer.size*2)
        buffer.forEachIndexed { index, v ->
            bytes[index*2]=(v.toInt() and 255).toByte()
            bytes[index*2+1]=((v.toInt() ushr 8) and 255).toByte()
        }
        synchronized(mutex) {
            if (!active) return
            if (blocks.offer(bytes)) { writeAccounting.accepted(bytes.size) }
            else { writeAccounting.rejected(); failed=true; active=false }
        }
    }

    /** Stop and publish the local WAV to the normal LFN-Scout audio archive.
     * Does not increment the rolling daily quota and does not initiate a cloud upload.
     */
    suspend fun stop(context:Context): AudioArchiveEntry? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val f:File
        val t:Thread?
        synchronized(mutex) {
            if (!active && outputFile == null) return@withContext null
            active=false
            f=outputFile ?: return@withContext null
            t=thread
        }
        t?.join(10000L)
        if (t?.isAlive == true) {
            // Never delete a file while its writer still owns the stream. Keep the
            // reference so a later stop can join and safely clean up the failed take.
            failed = true
            return@withContext null
        }
        val accounting = writeAccounting.snapshot()
        if (failed || samples <= 0L || !accounting.complete(f.length()) ||
            accounting.writtenBytes != samples * 2L) {
            f.delete()
            synchronized(mutex) { outputFile=null;writer=null;thread=null }
            return@withContext null
        }
        val count=samples
        val length=count*2L
        if (length>Int.MAX_VALUE.toLong()-44L) { f.delete();return@withContext null }
        RandomAccessFile(f,"rw").use { raf ->
            raf.seek(0)
            fun tag(v:String) { raf.write(v.toByteArray(Charsets.US_ASCII)) }
            fun le32(v:Long) { repeat(4) { raf.write(((v ushr (it*8)) and 255).toInt()) } }
            fun le16(v:Int) { raf.write(v and 255);raf.write((v ushr 8) and 255) }
            tag("RIFF");le32(length+36);tag("WAVEfmt ");le32(16);le16(1);le16(1)
            le32(RATE.toLong());le32((RATE*2).toLong());le16(2);le16(16)
            tag("data");le32(length)
        }
        val id="mon-${UUID.randomUUID()}"
        val endTime=OffsetDateTime.now()
        val now=startTime.toString()
        val base="MON_${startTime.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"))}" +
            "_FP${safe(firstParams.center)}Hz_FB${safe(firstParams.width)}Hz_G${safe(firstParams.gain)}dB"
        val metadata=JSONObject().apply {
            put("filter_position_hz",rounded(firstParams.center))
            put("filter_width_hz",rounded(firstParams.width))
            put("gain_db",rounded(firstParams.gain))
            put("sample_rate_hz",RATE)
            put("changes",JSONArray().apply {
                synchronized(mutex) {
                    changes.forEach { (t,v) ->
                        put(JSONObject().apply {
                            put("time_epoch_ms",t)
                            put("offset_seconds",((t-startTime.toInstant().toEpochMilli())/1000.0).coerceAtLeast(0.0))
                            put("filter_position_hz",rounded(v.center))
                            put("filter_width_hz",rounded(v.width))
                            put("gain_db",rounded(v.gain))
                        })
                    }
                }
            })
        }
        val archived=runCatching { AudioArchive.createRolling(
            context=context,recordingId=id,wavFile=f,
            targetDisplayName="$base.wav",recordedAt=now,
            gainDb=0,audioSource="UNP",clippedSamples=0,durationSeconds=(count/RATE).toInt(),
            startTime=now,endTime=endTime.toString(),deviceLabel="Mithör-Ausgang 48 kHz nach DSP",ratingTag="MON",
            origin="MON",isRolling=false,monMetadata=metadata)
        }.getOrNull()
        synchronized(mutex) { outputFile=null;writer=null;thread=null }
        archived
    }
}
