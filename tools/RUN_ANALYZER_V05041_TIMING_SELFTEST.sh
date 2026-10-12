#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
tmp=$(mktemp -d); trap 'rm -rf "$tmp"' EXIT
cat > "$tmp/JunitStub.kt" <<'KT'
package org.junit
annotation class Test
object Assert {
    fun assertTrue(value: Boolean) { if (!value) error("assertTrue failed") }
    fun assertEquals(expected: Int, actual: Int) { if (expected != actual) error("expected=$expected actual=$actual") }
    fun assertEquals(message: String, expected: Int, actual: Int) { if (expected != actual) error("$message expected=$expected actual=$actual") }
    fun assertEquals(expected: Double, actual: Double, tolerance: Double) { if (kotlin.math.abs(expected - actual) > tolerance) error("expected=$expected actual=$actual tol=$tolerance") }
}
KT
cat > "$tmp/Runner.kt" <<'KT'
import de.brummbeobachter.app.AnalyzerTimingTest
fun main() {
  val obj=AnalyzerTimingTest()
  val tests = listOf(
    "independentDecimationAndFftHop" to {obj.independentDecimationAndFftHop()},
    "smallerHopRecomputesWithoutChangingFrequencyBins" to {obj.smallerHopRecomputesWithoutChangingFrequencyBins()},
    "waterfallTimebaseIndependentOfFftHop" to {obj.waterfallTimebaseIndependentOfFftHop()},
    "widebandTimingUsesRealSampleRate" to {obj.widebandTimingUsesRealSampleRate()},
    "invalidHopsAreRejected" to {obj.invalidHopsAreRejected()},
    "restoredIdleModeNeverCrashesOnOldWidebandFactor" to {obj.restoredIdleModeNeverCrashesOnOldWidebandFactor()}
  )
  tests.forEach { (name,run) -> run(); println("PASS: $name") }
  println("HOST TIMING JUNIT-LOGIC PASS ${tests.size}/${tests.size} (JUnit replaced by compatible test-only stub)")
}
KT
kotlinc analyzer-core/src/main/kotlin/de/brummbeobachter/app/*.kt analyzer-core/src/test/kotlin/de/brummbeobachter/app/AnalyzerTimingTest.kt "$tmp/JunitStub.kt" "$tmp/Runner.kt" -include-runtime -d "$tmp/test.jar"
java -jar "$tmp/test.jar"
