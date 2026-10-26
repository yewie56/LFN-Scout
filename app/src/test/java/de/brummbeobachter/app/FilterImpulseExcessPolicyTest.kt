package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Test

class FilterImpulseExcessPolicyTest {
    @Test fun weakExcessAndThreshold() {
        assertEquals(0.0, FilterImpulseExcessPolicy.displayExcess(2.0,3),0.0001)
        assertEquals(3.0, FilterImpulseExcessPolicy.displayExcess(6.0,3),0.0001)
        assertEquals(10.0, FilterImpulseExcessPolicy.displayExcess(10.0,0),0.0001)
    }
    @Test fun clippingAndInvalid() {
        assertEquals(20.0,FilterImpulseExcessPolicy.displayExcess(50.0,5),0.0001)
        assertEquals(0.0,FilterImpulseExcessPolicy.displayExcess(Double.NaN,3),0.0001)
        assertEquals(0.0,FilterImpulseExcessPolicy.displayExcess(-10.0,3),0.0001)
    }
    @Test(expected=IllegalArgumentException::class) fun rejectBadThreshold() {
        FilterImpulseExcessPolicy.displayExcess(6.0,21)
    }
}
