package tag.egypt.com

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import tag.egypt.com.security.SecurityValidator
import java.io.File
import java.util.zip.ZipEntry

/**
 * Unit tests verifying security validation, URL normalization, and path traversal protection.
 */
class SecurityValidatorTest {

    @Test
    fun testValidUrlNormalization() {
        val normalized = SecurityValidator.validateAndNormalizeUrl("api.openai.com/v1///")
        assertEquals("https://api.openai.com/v1", normalized)

        val httpUrl = SecurityValidator.validateAndNormalizeUrl("http://localhost:11434/v1/")
        assertEquals("http://localhost:11434/v1", httpUrl)
    }

    @Test
    fun testMalformedUrlThrowsException() {
        assertThrows(IllegalArgumentException::class.java) {
            SecurityValidator.validateAndNormalizeUrl("   ")
        }

        assertThrows(IllegalArgumentException::class.java) {
            SecurityValidator.validateAndNormalizeUrl("ftp://ftp.secure-files.org")
        }
    }

    @Test
    fun testPrivateIpDetection() {
        assertTrue(SecurityValidator.isPrivateOrLocalHost("http://localhost:11434"))
        assertTrue(SecurityValidator.isPrivateOrLocalHost("http://127.0.0.1:8000"))
        assertTrue(SecurityValidator.isPrivateOrLocalHost("http://192.168.1.50:8080"))
        assertTrue(SecurityValidator.isPrivateOrLocalHost("http://10.0.0.2:8080"))
        assertFalse(SecurityValidator.isPrivateOrLocalHost("https://api.openai.com"))
        assertFalse(SecurityValidator.isPrivateOrLocalHost("https://api.anthropic.com"))
    }

    @Test
    fun testZipSlipPathTraversalBlocked() {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "zip_test_" + System.currentTimeMillis())
        tempDir.mkdirs()

        try {
            val maliciousEntry = ZipEntry("../../etc/passwd")
            assertThrows(SecurityException::class.java) {
                SecurityValidator.validateZipEntry(tempDir, maliciousEntry)
            }
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun testFileSizeLimitValidation() {
        // 5MB limit
        val maxAllowed = 5 * 1024 * 1024L
        SecurityValidator.validateFileSize(4 * 1024 * 1024L, maxAllowed) // Should pass

        assertThrows(IllegalArgumentException::class.java) {
            SecurityValidator.validateFileSize(6 * 1024 * 1024L, maxAllowed) // Should fail
        }
    }
}
