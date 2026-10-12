package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class RollingFftEpochEngineTest {
    @Test fun toneGoesToExpectedBand() {
        val result = mutableListOf<Pair<Double, Double>>()
        val engine = RollingFftEpochEngine { _, frequencies, power ->
            result += RollingFftEpochEngine.bandDb(frequencies, power, 30.0, 40.0) to
                RollingFftEpochEngine.bandDb(frequencies, power, 60.0, 70.0)
        }
        for (i in 0 until 3000) {
            val v = (0.15 * Short.MAX_VALUE * sin(2.0 * PI * 35.0 * i / 1000.0)).toInt().toShort()
            engine.accept(v, i.toLong())
        }
        engine.stop()
        assertEquals(3, result.size)
        assertTrue(result[1].first > result[1].second + 15.0)
    }
    @Test fun silenceRemainsAtFloor() {
        val data = mutableListOf<Double>()
        val engine = RollingFftEpochEngine { _, f, p ->
            data += RollingFftEpochEngine.bandDb(f, p, 2.0, 20.0)
        }
        for (i in 0 until 1000) engine.accept(0, i.toLong())
        engine.stop()
        assertEquals(1, data.size)
        assertTrue(data[0] <= -199.9)
    }
}
