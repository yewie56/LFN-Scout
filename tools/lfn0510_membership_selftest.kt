/** Kotlin compiler regression for Set<Int> versus IntArray in mode-specific hop checks. */
private val directHops: Set<Int> = setOf(15, 25, 30, 480)
private val rollingHops: IntArray = intArrayOf(5, 10, 50, 100, 480)
private fun validHop(direct: Boolean, hop: Int): Boolean =
    if (direct) hop in directHops else hop in rollingHops
fun main() {
    check(validHop(true, 15) && !validHop(false, 15))
    check(validHop(false, 5) && !validHop(true, 5))
    check(validHop(true, 480) && validHop(false, 480))
    check(!validHop(false, 12345) && !validHop(true, 12345))
    println("PASS 4/4: Kotlin set/array mode-specific membership")
}
