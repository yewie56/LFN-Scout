package de.brummbeobachter.app

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.SystemClock
import android.util.Log

/**
 * Brief and practically inaudible audio playback used to make an enabled Bluetooth
 * remote MediaSession eligible for Android's media-button selection.
 *
 * This never enables the analyzer, captures microphone audio, modifies MON, or
 * changes S18 mappings. Android ultimately chooses the media-button session;
 * other actively playing media applications may reclaim the buttons immediately.
 */
internal object RemoteMediaPriming {
    private const val TAG = "LFN-RemoteMediaPrime"
    private const val SAMPLE_RATE = 48000
    private const val DURATION_MS = 450L
    private const val MIN_INTERVAL_MS = 2500L
    private var lastAttemptMs = 0L
    private var running = false

    @Synchronized
    fun shouldStart(): Boolean {
        val now = SystemClock.elapsedRealtime()
        if (running || now - lastAttemptMs < MIN_INTERVAL_MS) return false
        running = true
        lastAttemptMs = now
        return true
    }

    fun playBriefAudio() {
        var track: AudioTrack? = null
        try {
            val sampleCount = (SAMPLE_RATE * DURATION_MS / 1000L).toInt()
            // Alternating single-LSB PCM samples: ~ -90 dBFS, almost inaudible.
            // Nonzero PCM is intentional; a zero-only buffer can be classified as silence.
            val samples = ShortArray(sampleCount) { if (it and 1 == 0) 1 else -1 }
            track = AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
                .setAudioFormat(AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build())
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(samples.size * 2)
                .build()
            val written = track.write(samples, 0, samples.size)
            if (written != samples.size) {
                Log.w(TAG, "Audiopuffer unvollständig: $written / ${samples.size}")
                return
            }
            track.play()
            Thread.sleep(DURATION_MS + 100L)
            Log.i(TAG, "Kurze Audioaktivierung abgeschlossen")
        } catch (ex: Exception) {
            Log.w(TAG, "Audioaktivierung fehlgeschlagen", ex)
        } finally {
            try { track?.stop() } catch (_: Exception) { }
            try { track?.release() } catch (_: Exception) { }
            synchronized(this) { running = false }
        }
    }
}
