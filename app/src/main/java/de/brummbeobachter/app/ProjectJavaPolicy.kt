package de.brummbeobachter.app

object ProjectJavaPolicy {
    const val JAVA_MAJOR_VERSION = 17
    const val RELATIVE_JAVA_HOME = ".tools/jdk17/current"
    const val DISCOVERY_CACHE_FILE = ".tools/java17.path"
    const val REUSE_EXISTING_JDK_FIRST = true
    const val DOWNLOAD_ONLY_AS_LAST_RESORT = true
}
