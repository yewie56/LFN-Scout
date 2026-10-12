import de.brummbeobachter.app.LiveAnalyzerCore
import kotlin.math.*
fun tone(hz:Double,n:Int)=ShortArray(n){(32767.0*.5*sin(2*PI*hz*it/1000)).roundToInt().toShort()}
val c=LiveAnalyzerCore(1000,1024,256)
val result=c.accept(tone(10.0,8192)).last()
val peak=result.liveDbFs.indices.maxByOrNull { result.liveDbFs[it] }!!
check(abs(result.frequenciesHz[peak]-10.0)<1.0) { "peak ${result.frequenciesHz[peak]}" }
check(abs(result.liveDbFs[peak]+6.02)<1.3) { "amplitude ${result.liveDbFs[peak]}" }
val a=LiveAnalyzerCore(1000,1024,256)
val b=LiveAnalyzerCore(1000,1024,256)
val pcm=tone(32.0,8192)
a.accept(pcm)
for (offset in pcm.indices step 37) b.accept(pcm,offset,minOf(37,pcm.size-offset))
val a2=a.accept(tone(32.0,256)).last()
val b2=b.accept(tone(32.0,256)).last()
check(a2.sequence==b2.sequence && a2.liveDbFs.indices.all { abs(a2.liveDbFs[it]-b2.liveDbFs[it])<1e-10 })
val z=LiveAnalyzerCore(1000,1024,256)
check(z.accept(ShortArray(1023)).isEmpty())
check(z.accept(shortArrayOf(Short.MAX_VALUE)).last().inputClipped)
check(!z.accept(ShortArray(256)).last().inputClipped)
check(z.accept(ShortArray(256)).last().liveDbFs.all { it.isFinite() })
val h=LiveAnalyzerCore(1000,1024,256)
val before=h.accept(tone(20.0,2048)).last()
val idx=before.frequenciesHz.indices.minByOrNull { abs(before.frequenciesHz[it]-20.0) }!!
h.resetMaxHold()
val after=h.accept(ShortArray(1024)).last()
check(after.maxHoldDbFs[idx]<before.maxHoldDbFs[idx])
h.reset()
check(h.accept(ShortArray(100)).isEmpty())
println("PASS: tone frequency, dBFS, chunk determinism, clip, finite floor, max hold/reset")
