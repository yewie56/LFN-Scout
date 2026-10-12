import de.brummbeobachter.app.FastFilterBank
import de.brummbeobachter.app.DirectAnalyzerConfig
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.abs

fun check51(name:String, condition:Boolean) { check(condition) { "FAIL $name" }; println("PASS $name") }
fun main() {
    val c=FastFilterBank.Config(0.0,100.0,10)
    check51("10 bands 0..100",c.widthHz==10.0 && c.low(0)==0.0 && c.high(9)==100.0)
    val c20=FastFilterBank.Config(10.0,60.0,20)
    check51("20 bands 10..60",c20.widthHz==2.5 && c20.low(10)==35.0)
    check51("2..40 bands valid",runCatching { FastFilterBank.Config(0.0,200.0,40) }.isSuccess)
    check51("invalid bands rejected",runCatching { FastFilterBank.Config(0.0,200.0,41) }.isFailure)
    check51("invalid band edges rejected",runCatching { FastFilterBank.Config(50.0,50.0,10) }.isFailure)
    check51("direct 5-ms interval",5 in DirectAnalyzerConfig.transformIntervalsMs)
    check51("direct 8000/8 5ms hop",DirectAnalyzerConfig.hop(8000,8,5)==5)
    val bank=FastFilterBank(c)
    var r: FastFilterBank.Levels?=null
    for(chunk in 0 until 220) {
        val signal=ShortArray(800) { i->(11000*sin(2*PI*35.0*(chunk*800+i)/8000.0)).toInt().toShort() }
        r=bank.accept(signal)
    }
    val result=requireNotNull(r)
    val loudest=result.absoluteDbFs.indices.maxByOrNull { result.absoluteDbFs[it] } ?: -1
    check51("35 Hz selects band 30..40",loudest==3)
    check51("all filter levels finite",result.absoluteDbFs.all { it.isFinite() } && result.excessDb.all { it.isFinite() })
    check51("steady sound not transient",result.excessDb.maxOrNull()!!<3.0)
    bank.reset()
    val zeros=requireNotNull(bank.accept(ShortArray(800)))
    check51("reset and silence valid",zeros.absoluteDbFs.all { it<=-230.0 })
    println("FILTERBANK_SELFTEST PASS 11/11")
}
