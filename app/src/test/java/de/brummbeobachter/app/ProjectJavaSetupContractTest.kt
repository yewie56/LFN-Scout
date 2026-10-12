package de.brummbeobachter.app

import org.junit.Assert.assertEquals
import org.junit.Test

class ProjectJavaSetupContractTest {
    @Test
    fun project_java_policy_uses_jdk17_and_reuses_existing_installations() {
        assertEquals(17, ProjectJavaPolicy.JAVA_MAJOR_VERSION)
        assertEquals(".tools/java17.path", ProjectJavaPolicy.DISCOVERY_CACHE_FILE)
        assertEquals(true, ProjectJavaPolicy.REUSE_EXISTING_JDK_FIRST)
        assertEquals(true, ProjectJavaPolicy.DOWNLOAD_ONLY_AS_LAST_RESORT)
    }
}
