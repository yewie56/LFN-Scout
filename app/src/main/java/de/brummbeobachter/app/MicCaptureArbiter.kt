package de.brummbeobachter.app

import java.io.Closeable

/**
 * One in-process AudioRecord owner. The legacy recorder and rolling service must acquire
 * an exclusive token BEFORE opening AudioRecord; the analyzer subscribes to their PCM hub.
 */
internal object MicCaptureArbiter {
    private val lock = Any()
    private var active: Token? = null
    val owner: String? get() = synchronized(lock) { active?.name }
    /** Non-null until process restart if the hardware release threw an exception. */
    val uncertainReleaseOwner: String? get() = synchronized(lock) { active?.takeIf { it.releaseUncertain }?.name }

    class Token internal constructor(internal val name: String) : Closeable {
        internal var releaseUncertain: Boolean = false
            private set

        /** Fail closed: do not grant another AudioRecord after release() itself failed. */
        fun markHardwareReleaseUncertain() = synchronized(lock) {
            if (active === this) releaseUncertain = true
        }

        override fun close() { synchronized(lock) {
            if (active === this && !releaseUncertain) active = null
        } }
    }

    fun tryAcquire(name: String): Token? = synchronized(lock) {
        require(name.isNotBlank())
        if (active != null) null else Token(name).also { active = it }
    }
}
