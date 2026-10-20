package de.brummbeobachter.app

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.log10
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Experimental pre-gain first-order frequency-bank diagnostics, separate from the overall 2–20 Hz path.
 * These bands are not calibrated measurement filters. Zero-Hz lower edge means low-pass including DC.
 * Each completed bucket is emitted once, even when the rolling analyzer shares the microphone.
 */
internal class RollingFilterHalfHourStatistics(
    private val sampleRate: Int = 1000,
    val minimumHz: Int = 0,
    val maximumHz: Int = 100,
    val bandCount: Int = 10,
    val method: Method = Method.LEGACY_IIR,
    private val onCompleted: (List<BandResult>) -> Unit = {},
    private val onEpoch: (Long, List<Double>) -> Unit = { _, _ -> }
) {
    enum class Method(val label: String) {
        LEGACY_IIR("Legacy-IIR (bisheriger CSV-Filter)"),
        BUTTERWORTH4("Butterworth HP2 + LP2 (CSV)"),
        FFT_HANN("FFT-Hann (1-s-Fenster, CSV)")
    }
    init {
        require(sampleRate > maximumHz * 2 && minimumHz >= 0 && maximumHz > minimumHz)
        require(bandCount == 10 || bandCount == 20)
    }
    data class BandResult(
        val startEpochMs: Long, val endEpochMs: Long, val bandIndex: Int,
        val minimumHz: Double, val maximumHz: Double, val validEpochs: Int,
        val leqDbfs: Double?, val l50Dbfs: Double?, val l90Dbfs: Double?, val qualityStatus: String
    )
    private class Band(val low: Double, val high: Double, sampleRate: Int, private val method: Method) {
        private val highPass = if (low > 0.0) Biquad.highPass(sampleRate.toDouble(), low) else null
        private val lowPass = Biquad.lowPass(sampleRate.toDouble(), high)

        private val hp = if (low > 0.0) exp(-2.0 * PI * low / sampleRate) else 0.0
        private val lp = exp(-2.0 * PI * high / sampleRate)
        private var prior = 0.0
        private var hpValue = 0.0
        private var lpValue = 0.0
        var energy = 0.0
        val levels = ArrayList<Double>(1800)
        fun accept(x: Double) {
            val passed = if (low > 0.0) {
                hpValue = hp * (hpValue + x - prior)
                hpValue
            } else x
            prior = x
            lpValue = (1.0 - lp) * passed + lp * lpValue
            val result = if (method == Method.BUTTERWORTH4) lowPass.process(highPass?.process(x) ?: x) else lpValue
            if (method != Method.FFT_HANN) energy += result * result
        }
        fun setFftEnergy(v: Double) { energy = v }
        fun finishEpoch(count: Int, sampleRate: Int): Double? {
            val db = if (count >= sampleRate * 9 / 10) 10.0 * log10((energy / count).coerceAtLeast(1e-20)) else null
            if (db != null) levels.add(db)
            energy = 0.0
            return db
        }
    }
    /** Standard biquad with Butterworth Q=1/sqrt(2), independently reset for each CSV session. */
    private class Biquad(private val b0: Double, private val b1: Double, private val b2: Double,
                         private val a1: Double, private val a2: Double) {
        private var z1=0.0; private var z2=0.0
        fun process(x: Double): Double {
            val y=b0*x+z1
            z1=b1*x-a1*y+z2; z2=b2*x-a2*y
            return y
        }
        companion object {
            private fun create(fs: Double, f: Double, high: Boolean): Biquad {
                val w=2.0*PI*f.coerceIn(0.01,fs*0.49)/fs
                val c=cos(w); val a=sin(w)/(sqrt(2.0)); val a0=1.0+a
                val k=if(high) (1.0+c)/2.0 else (1.0-c)/2.0
                return Biquad(k/a0, (if(high) -2.0*k else 2.0*k)/a0, k/a0,
                    (-2.0*c)/a0, (1.0-a)/a0)
            }
            fun highPass(fs: Double, f: Double)=create(fs,f,true)
            fun lowPass(fs: Double, f: Double)=create(fs,f,false)
        }
    }
    private val fftSamples = DoubleArray(1024)
    private var fftSampleCount = 0
    private val width = (maximumHz - minimumHz).toDouble() / bandCount
    private val bands = Array(bandCount) { i -> Band(minimumHz + width * i, minimumHz + width * (i + 1), sampleRate, method) }
    /** Instrumentation only for the independent Rolling CSV bank, never for Analyzer FFT/FAST. */
    data class Performance(val samples: Long, val meanMicrosPerSample: Double,
                           val peakMicrosPerSample: Double, val realtimePercent: Double,
                           val epochCount: Long, val peakEpochMicros: Double,
                           val totalMicros: Double, val p50EpochMicros: Double, val p95EpochMicros: Double, val p99EpochMicros: Double, val methodName: String,
                           val bandCount: Int, val sampleRate: Int)
    private var timedSamples = 0L
    private var totalNanoseconds = 0L
    private var peakNanoseconds = 0L
    private var timedEpochs = 0L
    private var peakEpochNanoseconds = 0L
    private var epochNanoseconds = 0L
    private var currentEpochSampleNanoseconds = 0L
    private val epochCosts = ArrayList<Long>(64)
    private fun percentile(p: Double): Double {
        if (epochCosts.isEmpty()) return 0.0
        val sorted = epochCosts.sorted()
        val ix = kotlin.math.ceil((sorted.size * p)).toInt().coerceIn(1, sorted.size) - 1
        return sorted[ix] / 1000.0
    }
    fun performance(): Performance = Performance(timedSamples,
        if (timedSamples > 0) totalNanoseconds.toDouble()/timedSamples/1000.0 else 0.0,
        peakNanoseconds/1000.0,
        if (timedSamples > 0) 100.0 * totalNanoseconds.toDouble() * sampleRate / (timedSamples * 1e9) else 0.0,
        timedEpochs, peakEpochNanoseconds/1000.0, totalNanoseconds/1000.0,
        percentile(0.50), percentile(0.95), percentile(0.99), method.name, bandCount, sampleRate)
    /** Windowed snapshot: totals reset to avoid an ever-growing mean masking performance regressions. */
    fun consumePerformanceWindow(): Performance = performance().also {
        timedSamples=0; totalNanoseconds=0; peakNanoseconds=0
        timedEpochs=0; peakEpochNanoseconds=0; epochNanoseconds=0; epochCosts.clear()
    }
    private var bucketStart = -1L
    private var lastFftEpochMs = -1L
    private val fftSecondPresent = BooleanArray(1800)
    private var epochStart = -1L
    private var samples = 0

    /** FFT-backed epoch injection: legacy filter-bank source retained, but never run in standard rolling service. */
    fun acceptFftEpoch(timeEpochMs: Long, levelsDb: List<Double>) {
        require(levelsDb.size == bandCount)
        // Match the primary FFT-second ledger: bad levels, old callbacks and
        // duplicate seconds must not increase filter-bank coverage.
        if (timeEpochMs < 0L || levelsDb.any { !it.isFinite() }) return
        val bucket = Math.floorDiv(timeEpochMs, 1_800_000L) * 1_800_000L
        if (bucketStart < 0) bucketStart = bucket
        if (bucket < bucketStart) return
        if (bucket != bucketStart) {
            completeBucket()
            bucketStart = bucket
            lastFftEpochMs = -1L
        }
        val second = Math.floorDiv(timeEpochMs, 1000L) * 1000L
        if (second <= lastFftEpochMs) return
        val slot = ((second - bucketStart) / 1000L).toInt()
        if (slot !in 0 until 1800 || fftSecondPresent[slot]) return
        fftSecondPresent[slot] = true
        lastFftEpochMs = second
        bands.forEachIndexed { i, band -> band.levels.add(levelsDb[i]) }
        onEpoch(second, levelsDb)
    }

    fun accept(sample: Short, timeEpochMs: Long) {
        val t0=System.nanoTime()
        val bucket = Math.floorDiv(timeEpochMs, 1_800_000L) * 1_800_000L
        if (bucketStart < 0) bucketStart = bucket
        if (bucket != bucketStart) {
            finishEpoch()
            completeBucket()
            bucketStart = bucket
        }
        val epoch = Math.floorDiv(timeEpochMs, 1000L) * 1000L
        if (epochStart >= 0 && epoch != epochStart) finishEpoch()
        epochStart = epoch
        val x = sample.toDouble() / 32768.0
        bands.forEach { it.accept(x) }
        if (fftSampleCount < fftSamples.size) fftSamples[fftSampleCount++] = x
        samples++
        val dt=System.nanoTime()-t0
        timedSamples++; totalNanoseconds+=dt; currentEpochSampleNanoseconds+=dt
        if(dt>peakNanoseconds) peakNanoseconds=dt
    }
    private fun finishEpoch() {
        val t0=System.nanoTime()
        if (samples > 0) {
            if (method == Method.FFT_HANN) {
                val powers = fftBandPowers()
                bands.forEachIndexed { i, band -> band.setFftEnergy(powers[i] * samples) }
            }
            val levels = bands.mapIndexed { index, band ->
                band.finishEpoch(samples, sampleRate)?.let { rawDb ->
                    NoiseFloorReference.compensateEpoch(
                        NoiseFloorReference.filterKey(sampleRate, method.name, bandCount, band.low, band.high), rawDb)
                }
            }
            if (levels.all { it != null }) onEpoch(epochStart, levels.filterNotNull())
        }
        samples = 0
        fftSampleCount = 0
        epochNanoseconds = System.nanoTime()-t0
        totalNanoseconds += epochNanoseconds
        timedEpochs++
        val epochTotal = epochNanoseconds + currentEpochSampleNanoseconds
        epochCosts.add(epochTotal)
        currentEpochSampleNanoseconds = 0L
        if (epochTotal > peakEpochNanoseconds) peakEpochNanoseconds=epochTotal
    }
    /** 1024-point radix-2 FFT of each (normally 1000-sample) 1-s epoch.
     * One-sided mean-square is normalized using actual Hann window energy;
     * no FFT calculation is performed in the analyzer path.
     */
    private fun fftBandPowers(): DoubleArray {
        val n=1024; val m=fftSampleCount
        val re=DoubleArray(n); val im=DoubleArray(n)
        var winEnergy=0.0
        if (m < 2) return DoubleArray(bandCount)
        for (i in 0 until m) {
            val w=0.5-0.5*cos(2.0*PI*i/(m-1))
            re[i]=fftSamples[i]*w; winEnergy+=w*w
        }
        var j=0
        for (i in 1 until n) {
            var bit=n shr 1
            while (j and bit != 0) { j=j xor bit; bit=bit shr 1 }
            j=j xor bit
            if (i<j) { val t=re[i]; re[i]=re[j]; re[j]=t }
        }
        var len=2
        while (len<=n) {
            val phi=-2.0*PI/len
            val wr=cos(phi); val wi=sin(phi)
            var start=0
            while(start<n) {
                var cr=1.0; var ci=0.0
                for(k in 0 until len/2) {
                    val u=start+k; val v=u+len/2
                    val tr=cr*re[v]-ci*im[v]; val ti=cr*im[v]+ci*re[v]
                    re[v]=re[u]-tr; im[v]=im[u]-ti
                    re[u]+=tr; im[u]+=ti
                    val tmp=cr*wr-ci*wi; ci=cr*wi+ci*wr; cr=tmp
                }
                start+=len
            }
            len=len shl 1
        }
        val power=DoubleArray(bandCount)
        for (k in 0..n/2) {
            val f=k.toDouble()*sampleRate/n
            val band=((f-minimumHz)/width).toInt()
            if (f < minimumHz || f >= maximumHz || band !in 0 until bandCount) continue
            val factor=if(k==0 || k==n/2) 1.0 else 2.0
            power[band]+=factor*(re[k]*re[k]+im[k]*im[k])/(n*winEnergy)
        }
        return power
    }
    private fun completeBucket() {
        if (bucketStart < 0) return
        val results = bands.mapIndexed { i, band ->
            val sorted = band.levels.sortedDescending()
            val n = sorted.size
            val energyAvg = if (n > 0) band.levels.sumOf { Math.pow(10.0, it / 10.0) } / n else 0.0
            fun percentile(p: Double) = if (n == 0) null else sorted[((n - 1) * p).toInt()]
            BandResult(bucketStart, bucketStart + 1_800_000L, i + 1,
                band.low, band.high, n,
                if (n == 0) null else 10.0 * log10(energyAvg.coerceAtLeast(1e-20)),
                percentile(0.50), percentile(0.90),
                when (n) { 0 -> "NO_DATA"; 1800 -> "COMPLETE"; else -> "PARTIAL" })
        }
        onCompleted(results)
        bands.forEach { it.levels.clear() }
        fftSecondPresent.fill(false)
    }
    fun stop() { finishEpoch(); completeBucket(); bucketStart = -1L; epochStart = -1L; lastFftEpochMs = -1L }
}
