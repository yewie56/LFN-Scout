package de.brummbeobachter.app

import org.junit.Assert.*
import org.junit.Test

class RecordingButtonPolicyTest {
    @Test fun fillingNeverImpliesCloudSuccess() {
        val x=RecordingButtonPolicy.view(75,120,2,5,RecordingButtonPolicy.Cloud.NONE)
        assertEquals("75/120",x.seconds)
        assertEquals("2/5 REC",x.recordings)
        assertEquals(RecordingButtonPolicy.Cloud.NONE,x.cloud)
    }
    @Test fun storedAndUploadedDifferOnlyInStatusIcon() {
        val pending=RecordingButtonPolicy.view(120,120,3,5,RecordingButtonPolicy.Cloud.PENDING)
        val confirmed=RecordingButtonPolicy.view(210,120,3,5,RecordingButtonPolicy.Cloud.CONFIRMED)
        assertEquals("120/120",pending.seconds)
        assertEquals(pending.seconds,confirmed.seconds)
        assertEquals(pending.recordings,confirmed.recordings)
        assertEquals(RecordingButtonPolicy.Cloud.CONFIRMED,confirmed.cloud)
    }
    @Test fun configurableMaximum() {
        assertEquals("165/180",RecordingButtonPolicy.view(165,180,0,5,RecordingButtonPolicy.Cloud.NONE).seconds)
        assertEquals("180/180",RecordingButtonPolicy.view(200,180,1,5,RecordingButtonPolicy.Cloud.NONE).seconds)
    }
    @Test fun resetRequiresFullPreviousWindow() {
        assertFalse(RecordingButtonPolicy.resetCloudOnNewBuffer(40,20,120))
        assertTrue(RecordingButtonPolicy.resetCloudOnNewBuffer(120,4,120))
        assertFalse(RecordingButtonPolicy.resetCloudOnNewBuffer(120,120,120))
    }
}
