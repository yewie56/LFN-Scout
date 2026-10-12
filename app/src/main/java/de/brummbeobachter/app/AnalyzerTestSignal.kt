package de.brummbeobachter.app

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

/** Synthetic PCM ONLY to analyzer raw hub. The rolling/WAV path never receives these blocks. */
internal object AnalyzerTestSignal {
    private var scope: CoroutineScope? = null
    private var job: Job? = null
    @Volatile private var generation: Long = 0L
    @Volatile var isRunning: Boolean = false
        private set

    fun start(context: Context, sampleRate: Int, direct: Boolean = false): Boolean {
        require(sampleRate in DirectAnalyzerConfig.inputRates)
        stop()
        val thisGeneration = generation
        val prefs = context.getSharedPreferences("analyzer_test_signal", Context.MODE_PRIVATE)
        val config = context.getSharedPreferences("lfn_scout_live_analyzer", Context.MODE_PRIVATE)
        val f1 = if (direct) config.getFloat("mode_48_direct_test1_hz",50f).toDouble() else prefs.getFloat("f1",50f).toDouble()
        val f2 = if (direct) config.getFloat("mode_48_direct_test2_hz",195f).toDouble() else prefs.getFloat("f2",195f).toDouble()
        val on1 = if (direct) config.getBoolean("mode_48_direct_test1_on",true) else true
        val on2 = if (direct) config.getBoolean("mode_48_direct_test2_on",false) else prefs.getBoolean("dual",false)
        val db = prefs.getInt("level_dbfs", -24)
        if ((!on1 && !on2) || (on1 && (f1 < 1.0 || f1 >= sampleRate/2.0)) ||
            (on2 && (f2 < 1.0 || f2 >= sampleRate/2.0)) || db !in -100..-1) return false
        val amplitude = 10.0.pow(db/20.0) / (if(on1 && on2) 2.0 else 1.0)
        val currentScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        scope = currentScope
        isRunning = true
        AnalyzerSessionGateway.rawPcm.markDiscontinuity()
        job = currentScope.launch {
            var index = 0L
            val frame = ShortArray(sampleRate / 50) // 20 ms
            try {
                while (isActive) {
                    val firstNs = System.nanoTime()
                    for (i in frame.indices) {
                        val n = index + i
                        val value = (if(on1) amplitude * sin(2.0*PI*f1*n/sampleRate) else 0.0) +
                            (if(on2) amplitude * sin(2.0*PI*f2*n/sampleRate) else 0.0)
                        frame[i] = (value * 32767).roundToInt().coerceIn(-32768,32767).toShort()
                    }
                    index += frame.size
                    if (AnalyzerSessionGateway.rawPcm.activeSubscribers > 0)
                        AnalyzerSessionGateway.rawPcm.publish(frame, frame.size, sampleRate,
                            "TESTSIGNAL (KEINE MESSUNG)", "Digital", 0, firstNs)
                    delay(20)
                }
            } finally {
                if (generation == thisGeneration) {
                    isRunning = false
                    AnalyzerSessionGateway.rawPcm.markDiscontinuity()
                }
            }
        }
        return true
    }

    fun stop() {
        generation++
        isRunning = false
        job?.cancel()
        job = null
        scope?.cancel()
        scope = null
    }
}
