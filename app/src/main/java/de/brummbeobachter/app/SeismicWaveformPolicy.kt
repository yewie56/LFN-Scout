package de.brummbeobachter.app

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.abs
import kotlin.math.max

data class MiniSeedTrace(
    val network: String,
    val station: String,
    val location: String,
    val channel: String,
    val startEpochMs: Long,
    val sampleRateHz: Double,
    val samples: IntArray,
    val recordCount: Int,
    val encodings: Set<Int>
) {
    val endEpochMs: Long
        get() = if (sampleRateHz > 0.0 && samples.isNotEmpty()) {
            startEpochMs + (((samples.size - 1).toDouble() / sampleRateHz) * 1000.0).toLong()
        } else {
            startEpochMs
        }
}

data class SeismicEnvelopeBin(
    val min: Float,
    val max: Float,
    val first: Float = min,
    val last: Float = max,
    val minBeforeMax: Boolean = true
)

data class SeismicEnvelope(
    val bins: List<SeismicEnvelopeBin>,
    val mean: Double,
    val maxAbs: Double
)

object SeismicWaveformPolicy {
    fun buildEnvelope(samples: IntArray, maxBins: Int = 900): SeismicEnvelope {
        if (samples.isEmpty() || maxBins <= 0) return SeismicEnvelope(emptyList(), 0.0, 0.0)
        val mean = samples.fold(0.0) { acc, value -> acc + value } / samples.size.toDouble()
        val binCount = minOf(maxBins, samples.size)
        val bins = ArrayList<SeismicEnvelopeBin>(binCount)
        var globalMax = 0.0
        for (bin in 0 until binCount) {
            val start = (bin.toLong() * samples.size / binCount).toInt()
            val end = (((bin + 1L) * samples.size / binCount).toInt()).coerceAtLeast(start + 1)
            var minValue = Double.POSITIVE_INFINITY
            var maxValue = Double.NEGATIVE_INFINITY
            var minIndex = start
            var maxIndex = start
            val actualEnd = minOf(end, samples.size)
            val firstValue = samples[start] - mean
            var lastValue = firstValue
            for (i in start until actualEnd) {
                val centered = samples[i] - mean
                if (centered < minValue) {
                    minValue = centered
                    minIndex = i
                }
                if (centered > maxValue) {
                    maxValue = centered
                    maxIndex = i
                }
                lastValue = centered
                globalMax = max(globalMax, abs(centered))
            }
            bins += SeismicEnvelopeBin(
                min = minValue.toFloat(),
                max = maxValue.toFloat(),
                first = firstValue.toFloat(),
                last = lastValue.toFloat(),
                minBeforeMax = minIndex <= maxIndex
            )
        }
        return SeismicEnvelope(bins, mean, globalMax)
    }
}

object MiniSeed2Decoder {
    fun decode(bytes: ByteArray): MiniSeedTrace {
        require(bytes.size >= 64) { "MiniSEED response too short" }

        data class RecordRef(val offset: Int, val header: Header)

        val records = ArrayList<RecordRef>()
        var offset = 0
        var totalSamples = 0L
        var network = ""
        var station = ""
        var location = ""
        var channel = ""
        var sampleRate = 0.0
        var firstStart = 0L
        val encodings = linkedSetOf<Int>()

        while (offset + 64 <= bytes.size) {
            val header = parseHeader(bytes, offset)
            if (header.recordLength <= 0 || offset + header.recordLength > bytes.size) {
                throw IllegalArgumentException("Invalid MiniSEED record length ${header.recordLength} at $offset")
            }
            if (records.isEmpty()) {
                firstStart = header.startEpochMs
                sampleRate = header.sampleRateHz
                network = header.network
                station = header.station
                location = header.location
                channel = header.channel
            } else if (
                header.network != network || header.station != station ||
                header.location != location || header.channel != channel
            ) {
                throw IllegalArgumentException("MiniSEED response contains multiple streams")
            }

            totalSamples += header.sampleCount.toLong()
            require(totalSamples <= Int.MAX_VALUE) { "Too many MiniSEED samples" }
            records += RecordRef(offset, header)
            encodings += header.encoding
            offset += header.recordLength
        }

        require(records.isNotEmpty() && totalSamples > 0L) { "No MiniSEED samples decoded" }
        val output = IntArray(totalSamples.toInt())
        var writeOffset = 0
        for (record in records) {
            val recordSamples = decodeRecord(bytes, record.offset, record.header)
            recordSamples.copyInto(output, destinationOffset = writeOffset)
            writeOffset += recordSamples.size
        }

        return MiniSeedTrace(
            network = network,
            station = station,
            location = location,
            channel = channel,
            startEpochMs = firstStart,
            sampleRateHz = sampleRate,
            samples = output,
            recordCount = records.size,
            encodings = encodings
        )
    }

    private data class Header(
        val network: String,
        val station: String,
        val location: String,
        val channel: String,
        val startEpochMs: Long,
        val sampleCount: Int,
        val sampleRateHz: Double,
        val dataOffset: Int,
        val encoding: Int,
        val dataBigEndian: Boolean,
        val recordLength: Int
    )

    private fun parseHeader(bytes: ByteArray, base: Int): Header {
        val b = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN)
        val station = ascii(bytes, base + 8, 5)
        val location = ascii(bytes, base + 13, 2)
        val channel = ascii(bytes, base + 15, 3)
        val network = ascii(bytes, base + 18, 2)
        val year = u16(b.getShort(base + 20))
        val day = u16(b.getShort(base + 22))
        val hour = bytes[base + 24].toInt() and 0xff
        val minute = bytes[base + 25].toInt() and 0xff
        val second = (bytes[base + 26].toInt() and 0xff).coerceAtMost(59)
        val fraction100us = u16(b.getShort(base + 28))
        val sampleCount = u16(b.getShort(base + 30))
        val factor = b.getShort(base + 32).toInt()
        val multiplier = b.getShort(base + 34).toInt()
        val dataOffset = u16(b.getShort(base + 44))
        var blocketteOffset = u16(b.getShort(base + 46))
        var encoding = -1
        var bigEndian = true
        var recordLength = 0
        var guard = 0
        while (blocketteOffset > 0 && guard++ < 32) {
            val p = base + blocketteOffset
            require(p + 4 <= bytes.size) { "MiniSEED blockette outside response" }
            val type = u16(b.getShort(p))
            val next = u16(b.getShort(p + 2))
            if (type == 1000) {
                require(p + 8 <= bytes.size) { "Truncated blockette 1000" }
                encoding = bytes[p + 4].toInt() and 0xff
                bigEndian = (bytes[p + 5].toInt() and 0xff) != 0
                val exponent = bytes[p + 6].toInt() and 0xff
                require(exponent in 8..20) { "Unsupported MiniSEED record exponent $exponent" }
                recordLength = 1 shl exponent
                break
            }
            if (next == blocketteOffset) break
            blocketteOffset = next
        }
        require(encoding >= 0 && recordLength > 0) { "MiniSEED blockette 1000 missing" }
        require(dataOffset in 48 until recordLength) { "Invalid MiniSEED data offset $dataOffset" }
        val date = LocalDate.ofYearDay(year, day)
        val start = date.atTime(hour, minute, second, fraction100us * 100_000)
            .toInstant(ZoneOffset.UTC).toEpochMilli()
        return Header(
            network, station, location, channel, start, sampleCount,
            sampleRate(factor, multiplier), dataOffset, encoding, bigEndian, recordLength
        )
    }

    private fun decodeRecord(bytes: ByteArray, base: Int, h: Header): IntArray {
        if (h.sampleCount == 0) return IntArray(0)
        val order = if (h.dataBigEndian) ByteOrder.BIG_ENDIAN else ByteOrder.LITTLE_ENDIAN
        val b = ByteBuffer.wrap(bytes).order(order)
        return when (h.encoding) {
            1 -> IntArray(h.sampleCount) { i -> b.getShort(base + h.dataOffset + i * 2).toInt() }
            3 -> IntArray(h.sampleCount) { i -> b.getInt(base + h.dataOffset + i * 4) }
            10 -> decodeSteim(bytes, base + h.dataOffset, h.recordLength - h.dataOffset, h.sampleCount, order, false)
            11 -> decodeSteim(bytes, base + h.dataOffset, h.recordLength - h.dataOffset, h.sampleCount, order, true)
            else -> throw IllegalArgumentException("Unsupported MiniSEED encoding ${h.encoding}")
        }
    }

    private fun decodeSteim(
        bytes: ByteArray,
        dataStart: Int,
        dataLength: Int,
        sampleCount: Int,
        order: ByteOrder,
        steim2: Boolean
    ): IntArray {
        require(dataLength >= 64) { "Truncated Steim data" }
        val b = ByteBuffer.wrap(bytes).order(order)
        val values = ArrayList<Int>(sampleCount)
        var current = 0
        var firstFrame = true
        var frameStart = dataStart
        while (frameStart + 64 <= dataStart + dataLength && values.size < sampleCount) {
            val control = b.getInt(frameStart)
            if (firstFrame) {
                current = b.getInt(frameStart + 4)
                values += current
            }
            for (wordIndex in 1..15) {
                if (values.size >= sampleCount) break
                if (firstFrame && (wordIndex == 1 || wordIndex == 2)) continue
                val code = (control ushr (30 - wordIndex * 2)) and 0x3
                if (code == 0) continue
                val word = b.getInt(frameStart + wordIndex * 4)
                val diffs = if (steim2) steim2Diffs(code, word) else steim1Diffs(code, word)
                for (diff in diffs) {
                    if (values.size >= sampleCount) break
                    current += diff
                    values += current
                }
            }
            firstFrame = false
            frameStart += 64
        }
        require(values.size >= sampleCount) { "Steim decoded ${values.size}/$sampleCount samples" }
        return values.take(sampleCount).toIntArray()
    }

    private fun steim1Diffs(code: Int, word: Int): IntArray = when (code) {
        1 -> intArrayOf(
            signExtend((word ushr 24) and 0xff, 8),
            signExtend((word ushr 16) and 0xff, 8),
            signExtend((word ushr 8) and 0xff, 8),
            signExtend(word and 0xff, 8)
        )
        2 -> intArrayOf(
            signExtend((word ushr 16) and 0xffff, 16),
            signExtend(word and 0xffff, 16)
        )
        3 -> intArrayOf(word)
        else -> IntArray(0)
    }

    private fun steim2Diffs(code: Int, word: Int): IntArray {
        if (code == 1) return steim1Diffs(1, word)
        val dnib = (word ushr 30) and 0x3
        return when (code) {
            2 -> when (dnib) {
                1 -> groups(word and 0x3fffffff, 1, 30)
                2 -> groups(word and 0x3fffffff, 2, 15)
                3 -> groups(word and 0x3fffffff, 3, 10)
                else -> IntArray(0)
            }
            3 -> when (dnib) {
                0 -> groups(word and 0x3fffffff, 5, 6)
                1 -> groups(word and 0x3fffffff, 6, 5)
                2 -> groups(word and 0x0fffffff, 7, 4)
                else -> IntArray(0)
            }
            else -> IntArray(0)
        }
    }

    private fun groups(payload: Int, count: Int, bits: Int): IntArray {
        val out = IntArray(count)
        for (i in 0 until count) {
            val shift = (count - 1 - i) * bits
            val mask = if (bits == 32) -1 else (1 shl bits) - 1
            out[i] = signExtend((payload ushr shift) and mask, bits)
        }
        return out
    }

    private fun signExtend(value: Int, bits: Int): Int {
        if (bits == 32) return value
        val shift = 32 - bits
        return (value shl shift) shr shift
    }

    private fun sampleRate(factor: Int, multiplier: Int): Double {
        if (factor == 0 || multiplier == 0) return 0.0
        return when {
            factor > 0 && multiplier > 0 -> factor.toDouble() * multiplier
            factor > 0 && multiplier < 0 -> factor.toDouble() / -multiplier
            factor < 0 && multiplier > 0 -> multiplier.toDouble() / -factor
            else -> 1.0 / (factor.toDouble() * multiplier.toDouble())
        }
    }

    private fun ascii(bytes: ByteArray, start: Int, length: Int): String =
        bytes.copyOfRange(start, start + length).toString(Charsets.US_ASCII).trim()

    private fun u16(value: Short): Int = value.toInt() and 0xffff
}
