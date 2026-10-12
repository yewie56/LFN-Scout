import de.brummbeobachter.app.WaterfallLevelPolicy

fun main() {
    val at50 = WaterfallLevelPolicy.effectiveManualRange(50, -100, -20)
    check(at50.minDb == -100.0 && at50.maxDb == -20.0)
    val at40 = WaterfallLevelPolicy.effectiveManualRange(40, -100, -20)
    check(at40.minDb == -110.0 && at40.maxDb == -30.0)
    val at30 = WaterfallLevelPolicy.effectiveManualRange(30, -100, -20)
    check(at30.minDb == -120.0 && at30.maxDb == -40.0)
    check((at50.maxDb - at50.minDb) == (at30.maxDb - at30.minDb))
    println("WaterfallLevelPolicySelfTest PASS")
}
