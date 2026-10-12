import de.brummbeobachter.app.*
import kotlin.math.*

fun checkClose(actual:Double, expected:Double, tolerance:Double, label:String) {
    check(abs(actual-expected) < tolerance) { "$label: $actual vs $expected" }
}
fun main() {
    val decimator=RawLfDecimator()
    val processor=LiveAnalyzerCore(1000,4096,512,200.0)
    var last:LiveAnalyzerCore.Spectrum?=null
    var samples=0
    // Low-level raw 8-kHz signal; deliberately less than one averaged short count.
    // Output must retain the signal in double precision.
    for(start in 0 until 80000 step 257) {
        val size=min(257,80000-start)
        val chunk=ShortArray(size) { i ->
            val t=(start+i).toDouble()/8000.0
            (2.0*sin(2*PI*47.0*t)+45.0*sin(2*PI*180.0*t)).toInt().toShort()
        }
        val lf=decimator.accept(chunk)
        samples+=lf.size
        processor.acceptNormalized(lf).lastOrNull()?.let {last=it}
    }
    val spectrum=checkNotNull(last)
    check(samples > 9000)
    val peak=spectrum.liveDbFs.indices.maxByOrNull { spectrum.liveDbFs[it] }!!
    checkClose(spectrum.frequenciesHz[peak],180.0,0.3,"strong 180Hz peak")
    val near47=spectrum.frequenciesHz.indices.minByOrNull {abs(spectrum.frequenciesHz[it]-47.0)}!!
    check(spectrum.liveDbFs[near47]>-100.0) { "quiet 47Hz suppressed: ${spectrum.liveDbFs[near47]}" }
    println("PASS: 8k→1k FIR low-level 47Hz and 180Hz; peak=${spectrum.frequenciesHz[peak]}Hz; 47Hz=${spectrum.liveDbFs[near47]}dBFS")
    // A 2.5kHz signal must not leak into the pass band after downsampling.
    val aliasDecimator=RawLfDecimator()
    val alias=ShortArray(80000){ i ->(8000*sin(2*PI*2500*i/8000.0)).toInt().toShort() }
    val filtered=aliasDecimator.accept(alias)
    val rms=sqrt(filtered.map {it*it}.average())*32768.0
    check(rms<4.0){"Alias not sufficiently suppressed: rms=$rms PCM"}
    println("PASS: 2500Hz alias suppressed, LF rms=$rms PCM")
    val zero=RawLfDecimator().accept(ShortArray(8192))
    check(zero.all {it==0.0})
    println("PASS: zero input yields exact zeros (no invented spectrum)")
    val pcm=AnalyzerPcmHub()
    var diags=0
    var spectra=0
    val session=AnalyzerRawLfSession(pcm,{spectra++},{diags++},200.0)
    val x=ShortArray(2048){i->(10000*sin(2*PI*60*i/8000.0)).toInt().toShort()}
    repeat(35){pcm.publish(x,x.size,8000,"UNPROCESSED","fixture",0); Thread.sleep(35)}
    Thread.sleep(1000)
    check(diags>0 && spectra>0) {"session dead: diags=$diags spectra=$spectra"}
    check(session.diagnostics?.sourceRateHz==8000)
    println("PASS: shared raw hub -> LF spectra and diagnostics; frames=$diags spectra=$spectra drops=${session.droppedFrames}")
    session.close()
}
