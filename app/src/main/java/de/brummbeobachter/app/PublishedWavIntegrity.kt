package de.brummbeobachter.app

import java.io.InputStream
import java.security.MessageDigest

/** Independent byte comparison after publication. Never assumes copyTo == persistence. */
internal object PublishedWavIntegrity {
    data class Fingerprint(val bytes: Long, val sha256: String)

    fun fingerprint(input: InputStream): Fingerprint {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(32768)
        var count = 0L
        input.use { stream ->
            while (true) {
                val n = stream.read(buffer)
                if (n == -1) break
                if (n == 0) continue
                count += n
                digest.update(buffer, 0, n)
            }
        }
        return Fingerprint(count, digest.digest().joinToString("") { "%02x".format(it) })
    }

    fun verify(source: InputStream, published: InputStream) {
        val expected = fingerprint(source)
        val actual = fingerprint(published)
        check(expected.bytes == actual.bytes && expected.sha256 == actual.sha256) {
            "Published WAV differs from source: expected=$expected actual=$actual"
        }
    }
}
