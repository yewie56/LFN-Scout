package de.brummbeobachter.app

import kotlin.math.abs

fun main() {
    var passed=0
    fun checkTest(name: String, ok: Boolean) {
        check(ok) { "FAIL $name" }
        passed++
        println("PASS $name")
    }
    val c=FilterDisplayRange(5)
    checkTest("empty result is unavailable",c.result()==null)
    c.accept(doubleArrayOf(-110.0,-100.0,-92.0))
    checkTest("one frame is insufficient",c.result()==null)
    c.accept(doubleArrayOf(-109.0,-98.0,-93.0))
    val first=c.result()
    checkTest("small band collection range valid",first!=null && first.first<first.second && first.second<=50)
    c.accept(doubleArrayOf(-112.0,-93.0,-45.0))
    val burst=c.result()
    checkTest("isolated valid peak retained",burst!=null && burst.second>=-40)
    checkTest("repeated computations deterministic",c.result()==burst)
    val bad=FilterDisplayRange()
    bad.accept(doubleArrayOf(-240.0,Double.NaN,Double.POSITIVE_INFINITY))
    bad.accept(doubleArrayOf(-240.0,Double.NEGATIVE_INFINITY))
    checkTest("invalid data does not set range",bad.result()==null)
    val constant=FilterDisplayRange(0)
    constant.accept(doubleArrayOf(-65.0,-65.0)); constant.accept(doubleArrayOf(-65.0,-65.0))
    val cr=constant.result()
    checkTest("constant signal gets at least 10 dB",cr!=null && cr.second-cr.first>=10)
    val low=FilterDisplayRange(30)
    low.accept(doubleArrayOf(-199.0,-198.0));low.accept(doubleArrayOf(-198.0,-197.0))
    checkTest("lower bound remains constrained",low.result()?.first in -240..40)
    val high=FilterDisplayRange(30)
    high.accept(doubleArrayOf(49.0,50.0));high.accept(doubleArrayOf(49.0,50.0))
    checkTest("upper bound remains constrained",high.result()?.second==50)
    checkTest("invalid margin rejected",runCatching{FilterDisplayRange(31)}.isFailure)
    println("FILTER AUTORANGE HOST PASS $passed/10")
}
