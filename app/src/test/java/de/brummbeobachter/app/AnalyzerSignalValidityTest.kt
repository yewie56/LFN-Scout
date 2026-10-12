package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Test

class AnalyzerSignalValidityTest {
    @Test fun compensatedAllZeroStillHasValidOriginalInput() {
        assertEquals(AnalyzerSignalValidity.Result.BELOW_NOISE_FLOOR,
            AnalyzerSignalValidity.classify(doubleArrayOf(-80.0,-95.0),doubleArrayOf(-180.0,-180.0),true))
    }
    @Test fun validPcmAtDisplayFloorIsNotAnInputFailure() {
        assertEquals(AnalyzerSignalValidity.Result.BELOW_NOISE_FLOOR,
            AnalyzerSignalValidity.classify(doubleArrayOf(-180.0,-180.0),doubleArrayOf(-180.0,-180.0),true))
    }
    @Test fun noFiniteOriginalSpectrumIsInvalid() {
        assertEquals(AnalyzerSignalValidity.Result.NO_INPUT,
            AnalyzerSignalValidity.classify(doubleArrayOf(Double.NaN, Double.POSITIVE_INFINITY),doubleArrayOf(-180.0),true))
        assertEquals(AnalyzerSignalValidity.Result.NO_INPUT,
            AnalyzerSignalValidity.classify(doubleArrayOf(),doubleArrayOf(),true))
    }
    @Test fun alternatingBelowFloorAndDetectableResidualNeverMeansNoInput() {
        val original = doubleArrayOf(-180.0, -180.0)
        val first = AnalyzerSignalValidity.classify(original,doubleArrayOf(-180.0,-180.0),true)
        val second = AnalyzerSignalValidity.classify(original,doubleArrayOf(-140.0,-180.0),true)
        assertEquals(AnalyzerSignalValidity.Result.BELOW_NOISE_FLOOR,first)
        assertEquals(AnalyzerSignalValidity.Result.VALID_SIGNAL,second)
    }
    @Test fun usefulResidualIsValid() {
        assertEquals(AnalyzerSignalValidity.Result.VALID_SIGNAL,
            AnalyzerSignalValidity.classify(doubleArrayOf(-85.0,-90.0),doubleArrayOf(-120.0,-180.0),true))
    }
    @Test fun noiseOffRemainsUnchanged() {
        assertEquals(AnalyzerSignalValidity.Result.VALID_SIGNAL,
            AnalyzerSignalValidity.classify(doubleArrayOf(-85.0),doubleArrayOf(-85.0),false))
    }
    @Test fun nonFiniteMixedWithMeasurablePowerIsInvalid() {
        assertEquals(AnalyzerSignalValidity.Result.NO_INPUT,
            AnalyzerSignalValidity.classify(doubleArrayOf(-85.0, Double.NaN), doubleArrayOf(-100.0), true))
        assertEquals(AnalyzerSignalValidity.Result.NO_INPUT,
            AnalyzerSignalValidity.classify(doubleArrayOf(-85.0, Double.NEGATIVE_INFINITY), doubleArrayOf(-100.0), false))
    }
    @Test fun validFloorValuesWithNoiseOffRemainInput() {
        assertEquals(AnalyzerSignalValidity.Result.VALID_SIGNAL,
            AnalyzerSignalValidity.classify(doubleArrayOf(-180.0, -180.0), doubleArrayOf(-180.0), false))
    }
    @Test fun invalidNumbersDoNotCreateFakeInput() {
        assertEquals(AnalyzerSignalValidity.Result.NO_INPUT,
            AnalyzerSignalValidity.classify(doubleArrayOf(Double.NaN,Double.POSITIVE_INFINITY,-180.0),doubleArrayOf(-180.0),true))
    }
}
