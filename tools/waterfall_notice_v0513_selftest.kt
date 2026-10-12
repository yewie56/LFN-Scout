package de.brummbeobachter.app

fun main() {
    var passed = 0
    fun checkCase(name: String, condition: Boolean) {
        check(condition) { "FAIL: $name" }
        passed++
        println("PASS: $name")
    }
    fun message(direct: Boolean = false, original: Boolean = true, filter: Boolean = false,
        mode: String = "MAXIMUM", bands: Int = 10, min: Double = 0.0, max: Double = 100.0,
        auto: Boolean = false): AnalyzerWaterfallNotice.Content = AnalyzerWaterfallNotice.content(
        mode, 120, 1000, 5, 100, direct, original, filter, bands, min, max, auto)
    val original = message()
    checkCase("original line names Wasserfall and MAX", original.first.contains("Wasserfall MAX"))
    checkCase("normal line contains sample rate and hop", original.first.contains("1000Hz") && original.first.contains("Hop 5"))
    checkCase("normal second line identifies interval and ORIGINAL", original.second.contains("100ms/Zeile") && original.second.contains("ORIGINAL"))
    checkCase("normal notice is not filter-colored", !original.isFilter)
    val filter = message(original=false, filter=true, bands=20,min=15.0,max=80.0)
    checkCase("filter only shows band number and actual frequency range", filter.second.contains("20 Bänder") && filter.second.contains("15–80Hz"))
    checkCase("filter only displays dBFS rather than relative overlay", filter.second.contains("RMS dBFS") && filter.isFilter)
    checkCase("both visible label as Overlay", message(original=true,filter=true).second.contains("Overlay"))
    checkCase("active autorange replaces second line without exposing live progress", message(filter=true,auto=true).second == "FILTER: Pegelautomatik läuft")
    checkCase("direct analyzer never claims filter, even stale filter flag", !message(direct=true,filter=true).isFilter)
    checkCase("waterfall switched off describes AUS", message(original=false,filter=false).second.contains("AUS"))
    checkCase("switch current vs average is explicit", message(mode="CURRENT").first.contains("Akt") && message(mode="AVERAGE").first.contains("MW"))
    println("WATERFALL_NOTICE_HOST PASS $passed/11")
}
