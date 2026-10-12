import de.brummbeobachter.app.StatisticsWindowEngine
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

fun main() {
    val raw = ShortArray(10_000) { i -> (60.0*sin(2.0*PI*10*i/1000)).roundToInt().toShort() }
    val boosted = ShortArray(raw.size) { i -> (raw[i]*10.0.pow(50.0/20)).roundToInt().toShort() }
    val cfg = StatisticsWindowEngine.Configuration(2.0,25.0,0.0,100.0,40)
    val baseline=StatisticsWindowEngine.analyze(raw,0.0,cfg)
    val result=StatisticsWindowEngine.analyze(boosted,50.0,cfg)
    check(result.bands.size==40)
    check(kotlin.math.abs(result.overall.leq-baseline.overall.leq)<0.2)
    check(kotlin.math.abs(result.overall.l90-baseline.overall.l90)<0.2)
    result.bands.indices.forEach { i -> check(kotlin.math.abs(result.bands[i].levels.leq-baseline.bands[i].levels.leq)<0.2) }
    check(result.dominantFrequencyHz != null && kotlin.math.abs(result.dominantFrequencyHz!!-10.0)<1.0)
    println("PASS: 40 bands, gain +50 dB compensation, L90, Leq, dominant tone")
}
