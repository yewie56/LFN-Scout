package de.brummbeobachter.app

import java.nio.ByteBuffer
import java.nio.ByteOrder

object SeismicWaveformSelfTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val samples = intArrayOf(100, 105, 103, 110, 90, 95)
        val record = makeInt32MiniSeed(samples, sampleRate = 20)
        val decoded = MiniSeed2Decoder.decode(record)
        check(decoded.samples.contentEquals(samples)) { "INT32 decode mismatch: ${decoded.samples.joinToString()}" }
        check(kotlin.math.abs(decoded.sampleRateHz - 20.0) < 1e-9) { "sample rate mismatch" }
        check(decoded.channel == "BHZ") { "channel mismatch" }

        val steim1Samples = intArrayOf(100, 101, 99, 102, 100)
        val steim1 = MiniSeed2Decoder.decode(makeSteim1MiniSeed(steim1Samples))
        check(steim1.samples.contentEquals(steim1Samples)) { "Steim1 decode mismatch: ${steim1.samples.joinToString()}" }

        val steim2Samples = intArrayOf(100, 105, 103, 110)
        val steim2 = MiniSeed2Decoder.decode(makeSteim2MiniSeed(steim2Samples))
        check(steim2.samples.contentEquals(steim2Samples)) { "Steim2 decode mismatch: ${steim2.samples.joinToString()}" }

        val multiFrameExpected = intArrayOf(100, 101, 102, 103, 104, 105, 106, 107, 108)
        val multiFrame = MiniSeed2Decoder.decode(makeMultiFrameSteim1MiniSeed())
        check(multiFrame.samples.contentEquals(multiFrameExpected)) {
            "Steim1 multi-frame mismatch: ${multiFrame.samples.joinToString()}"
        }

        val envelope = SeismicWaveformPolicy.buildEnvelope(decoded.samples, maxBins = 3)
        check(envelope.bins.size == 3) { "envelope bin count" }
        check(envelope.maxAbs > 0.0) { "envelope amplitude" }

        val stationText = """
            #Network|Station|Location|Channel|Latitude|Longitude|Elevation|Depth|Azimuth|Dip|SensorDescription|Scale|ScaleFreq|ScaleUnits|SampleRate|StartTime|EndTime
            GE|STU||BHZ|48.7719|9.1950|360|0|0|-90|Sensor||||20|2020-01-01T00:00:00|
            GE|STU||HHZ|48.7719|9.1950|360|0|0|-90|Sensor||||100|2020-01-01T00:00:00|
            GE|FAR||HHZ|50.0000|10.0000|100|0|0|-90|Sensor||||100|2020-01-01T00:00:00|
        """.trimIndent()
        val parsed = SeismicStationPolicy.parseFdsnChannelText(stationText, 48.77, 9.19)
        check(parsed.channels.first().station.station == "STU") { "nearest station not first" }
        check(parsed.channels.first().channel == "HHZ") { "preferred vertical channel not first" }
        check(parsed.channels.all { it.channel.endsWith("Z") }) { "non-Z channel admitted" }

        println("PASS: SeismicWaveformSelfTest")
    }

    private fun makeMultiFrameSteim1MiniSeed(): ByteArray {
        val bytes = makeHeader(9, 10)
        val b = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
        // Frame 0: W3 contains four +1 differences.
        b.putInt(64, 1 shl 24)
        b.putInt(68, 100)
        b.putInt(72, 108)
        b.putInt(76, 0x01010101)
        // Frame 1: W1 contains four +1 differences. W1 control nibble is bits 29..28.
        b.putInt(128, 1 shl 28)
        b.putInt(132, 0x01010101)
        return bytes
    }

    private fun makeSteim1MiniSeed(samples: IntArray): ByteArray {
        require(samples.contentEquals(intArrayOf(100, 101, 99, 102, 100)))
        val bytes = makeHeader(samples.size, 10)
        val b = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
        b.putInt(64, 1 shl 24) // word 3 control nibble: bits 25..24
        b.putInt(68, samples.first())
        b.putInt(72, samples.last())
        val diffs = intArrayOf(1, -2, 3, -2)
        val word = ((diffs[0] and 0xff) shl 24) or ((diffs[1] and 0xff) shl 16) or
            ((diffs[2] and 0xff) shl 8) or (diffs[3] and 0xff)
        b.putInt(76, word)
        return bytes
    }

    private fun makeSteim2MiniSeed(samples: IntArray): ByteArray {
        require(samples.contentEquals(intArrayOf(100, 105, 103, 110)))
        val bytes = makeHeader(samples.size, 11)
        val b = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
        b.putInt(64, 2 shl 24) // word 3 control nibble: bits 25..24
        b.putInt(68, samples.first())
        b.putInt(72, samples.last())
        val diffs = intArrayOf(5, -2, 7)
        val payload = ((diffs[0] and 0x3ff) shl 20) or ((diffs[1] and 0x3ff) shl 10) or (diffs[2] and 0x3ff)
        b.putInt(76, (3 shl 30) or payload) // dnib=3 => 3 x 10-bit
        return bytes
    }

    private fun makeHeader(sampleCount: Int, encoding: Int): ByteArray {
        val bytes = ByteArray(512) { 0 }
        fun putAscii(offset: Int, width: Int, value: String) {
            value.padEnd(width, ' ').take(width).toByteArray(Charsets.US_ASCII).copyInto(bytes, offset)
        }
        putAscii(0, 6, "000001")
        bytes[6] = 'D'.code.toByte()
        putAscii(8, 5, "STU")
        putAscii(13, 2, "")
        putAscii(15, 3, "BHZ")
        putAscii(18, 2, "GE")
        val b = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
        b.putShort(20, 2026.toShort())
        b.putShort(22, 260.toShort())
        bytes[24] = 12
        b.putShort(30, sampleCount.toShort())
        b.putShort(32, 20)
        b.putShort(34, 1)
        bytes[39] = 1
        b.putShort(44, 64)
        b.putShort(46, 48)
        b.putShort(48, 1000)
        bytes[52] = encoding.toByte()
        bytes[53] = 1
        bytes[54] = 9
        return bytes
    }

    private fun makeInt32MiniSeed(samples: IntArray, sampleRate: Short): ByteArray {
        val bytes = ByteArray(512) { 0 }
        fun putAscii(offset: Int, width: Int, value: String) {
            val padded = value.padEnd(width, ' ').take(width).toByteArray(Charsets.US_ASCII)
            padded.copyInto(bytes, offset)
        }
        putAscii(0, 6, "000001")
        bytes[6] = 'D'.code.toByte()
        putAscii(8, 5, "STU")
        putAscii(13, 2, "")
        putAscii(15, 3, "BHZ")
        putAscii(18, 2, "GE")
        val b = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
        b.putShort(20, 2026.toShort())
        b.putShort(22, 260.toShort())
        bytes[24] = 12
        bytes[25] = 0
        bytes[26] = 0
        b.putShort(28, 0)
        b.putShort(30, samples.size.toShort())
        b.putShort(32, sampleRate)
        b.putShort(34, 1)
        bytes[39] = 1
        b.putShort(44, 64)
        b.putShort(46, 48)
        b.putShort(48, 1000)
        b.putShort(50, 0)
        bytes[52] = 3 // 32-bit integer
        bytes[53] = 1 // big endian
        bytes[54] = 9 // 2^9 = 512 bytes
        var p = 64
        samples.forEach { sample -> b.putInt(p, sample); p += 4 }
        return bytes
    }
}
