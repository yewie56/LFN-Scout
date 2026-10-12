#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT
mkdir -p "$tmp/android/graphics"
cat > "$tmp/android/graphics/Color.kt" <<'KT'
package android.graphics
object Color {
    fun rgb(r:Int,g:Int,b:Int) = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
}
KT
cat > "$tmp/FilterRenderTest.kt" <<'KT'
package de.brummbeobachter.app
fun main() {
    val db=doubleArrayOf(-120.0,-80.0,-30.0,-90.0,-90.0,-90.0,-90.0,-90.0,-90.0,-90.0)
    val exc=DoubleArray(db.size) { 16.0 }
    val base=0xFF010203.toInt()
    fun render(flo:Int,fhi:Int,orig:Boolean,fil:Boolean):Int =
        FilterWaterfallRenderer.colorAt(25.0,base,db,exc,orig,fil,0.0,100.0,-120,-60,flo,fhi)
    check(render(-120,-60,false,true)!=render(-90,-20,false,true))
    println("PASS independent FILTER color normalization")
    check(render(-120,-60,true,false)==base)
    println("PASS original-only pixel independent of FILTER range")
    check(render(-120,-60,false,false)==android.graphics.Color.rgb(19,12,41))
    println("PASS both-off empty background")
    check(render(-120,-60,true,true)!=base)
    println("PASS transient overlay enabled")
    val noTransient=FilterWaterfallRenderer.colorAt(25.0,base,db,DoubleArray(db.size),true,true,0.0,100.0,-120,-60,-120,-60)
    check(noTransient==base)
    println("PASS stationary background keeps original")
    println("FILTER RENDER HOST PASS 5/5")
}
KT
kotlinc "$tmp/android/graphics/Color.kt" app/src/main/java/de/brummbeobachter/app/FilterWaterfallRenderer.kt "$tmp/FilterRenderTest.kt" -include-runtime -d "$tmp/renderer.jar"
java -jar "$tmp/renderer.jar"
