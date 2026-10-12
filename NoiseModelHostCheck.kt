package de.brummbeobachter.app
import kotlin.math.*
fun main() {
    val fs=1000;val fft=4096
    val base=DoubleArray(fft/2+1) { i ->
        if(i==0) 1e-8 else 1e-10+1e-6/(i*fs.toDouble()/fft).pow(1.97)
    }
    val fit=checkNotNull(NoisePowerModel.fit(base,fs,fft))
    check(abs(fit.alpha-1.97)<0.2) { "alpha=${fit.alpha}" }
    val model=checkNotNull(NoisePowerModel.reference(base,fs,fft))
    check(model[0]==base[0]);check(model.all {it>=0 && it.isFinite()})
    val injected=1e-5
    val idx=40
    val rest=(base[idx]+injected-model[idx]).coerceAtLeast(0.0)
    check(abs(rest/injected-1)<0.05)
    val white=DoubleArray(base.size) { 3e-9 }
    val whiteFit=NoisePowerModel.fit(white,fs,fft)
    check(whiteFit == null) { "Flat white noise must not activate 1/f model" }
    // A flat reference cannot justify a 1/f assumption; it may return null or alpha with near-zero error.
    println("PASS: fitted alpha=${fit.alpha}, RMS=${fit.rmsLogErrorDb}, injected residual=$rest, flat=$whiteFit")
}
