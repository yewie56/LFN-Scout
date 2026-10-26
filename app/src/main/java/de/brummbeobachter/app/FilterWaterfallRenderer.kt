package de.brummbeobachter.app

import android.graphics.Color
import kotlin.math.max

/** Color contract shared by live view and offscreen PNG/JPG export. */
internal object FilterWaterfallRenderer {
    // Explicit filter-only LUT: black -> purple -> magenta -> pink -> white.
    private val palette = intArrayOf(0xFF08000EL.toInt(), 0xFF400052L.toInt(),
        0xFFA800A8L.toInt(), 0xFFEC40C8L.toInt(), 0xFFFFB9E9L.toInt(), 0xFFFFFFFFL.toInt())
    // Same six brightness stops as violet, mapped to dark/bright green; white stays white.
    private val greenPalette = intArrayOf(0xFF001008.toInt(), 0xFF004423.toInt(),
        0xFF008C43.toInt(), 0xFF38D56B.toInt(), 0xFFA8F6B0.toInt(), 0xFFFFFFFF.toInt())
    private fun blend(a: Int, b: Int, t: Double): Int {
        val k=t.coerceIn(0.0,1.0)
        fun channel(n: Int): Int = ((a shr n and 255)*(1-k)+(b shr n and 255)*k).toInt().coerceIn(0,255)
        return Color.rgb(channel(16),channel(8),channel(0))
    }
    fun filterColor(dbFs: Double, lowDb: Int, highDb: Int, green: Boolean = false): Int {
        val palette = if (green) greenPalette else this.palette
        val q=((dbFs-lowDb)/(highDb-lowDb).toDouble()).coerceIn(0.0,1.0)*(palette.size-1)
        val i=q.toInt().coerceIn(0,palette.lastIndex-1)
        return blend(palette[i],palette[i+1],q-i)
    }
    fun bandIndex(hz: Double, minHz: Double, maxHz: Double, bands: Int): Int? {
        if(hz<minHz || hz>maxHz || bands<=0) return null
        return ((hz-minHz)/(maxHz-minHz)*bands).toInt().coerceIn(0,bands-1)
    }
    fun colorAt(hz: Double, base: Int, db: DoubleArray?, excess: DoubleArray?,
                original: Boolean, filter: Boolean, minHz: Double, maxHz: Double,
                minDb: Int, maxDb: Int, filterMinDb: Int, filterMaxDb: Int, green: Boolean = false): Int {
        if(!filter || db==null || excess==null) return if(original)base else Color.rgb(19,12,41)
        val b=bandIndex(hz,minHz,maxHz,db.size) ?: return if(original)base else Color.rgb(19,12,41)
        if(!original) return filterColor(db[b],filterMinDb,filterMaxDb,green)
        // Show only transients above the slow adaptive baseline; preserve stable thin SLOW lines.
        val alpha=((excess[b]-3.0)/15.0).coerceIn(0.0,0.85)
        return blend(base,filterColor(db[b],filterMinDb,filterMaxDb,green),alpha)
    }
}
