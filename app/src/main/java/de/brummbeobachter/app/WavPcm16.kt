package de.brummbeobachter.app

import java.nio.ByteBuffer
import java.nio.ByteOrder

object WavPcm16 {

    fun buildHeader(
        sampleRateHz: Int,
        channels: Int,
        bitsPerSample: Int,
        dataBytes: Int
    ): ByteArray {
        require(sampleRateHz > 0)
        require(channels > 0)
        require(bitsPerSample == 16)
        require(dataBytes >= 0)

        val byteRate =
            sampleRateHz *
                channels *
                bitsPerSample / 8

        val blockAlign =
            channels *
                bitsPerSample / 8

        return ByteBuffer
            .allocate(44)
            .order(ByteOrder.LITTLE_ENDIAN)
            .apply {
                put("RIFF".toByteArray(Charsets.US_ASCII))
                putInt(36 + dataBytes)
                put("WAVE".toByteArray(Charsets.US_ASCII))
                put("fmt ".toByteArray(Charsets.US_ASCII))
                putInt(16)
                putShort(1.toShort())
                putShort(channels.toShort())
                putInt(sampleRateHz)
                putInt(byteRate)
                putShort(blockAlign.toShort())
                putShort(bitsPerSample.toShort())
                put("data".toByteArray(Charsets.US_ASCII))
                putInt(dataBytes)
            }
            .array()
    }
}
