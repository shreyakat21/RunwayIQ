package com.runwayiq.data

import java.io.File
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Encrypts secrets (like the Groq API key) before they're written to the OS
 * preference store, so they don't sit there as plain text (e.g. visible in a
 * Windows registry export or a Preferences backup file). The AES key lives in
 * its own file, chmod'd to the current user only, separate from the encrypted
 * value itself.
 */
object SecureKeyStore {
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val KEY_SIZE_BYTES = 32
    private const val IV_SIZE_BYTES = 12
    private const val GCM_TAG_BITS = 128

    private val keyFile: File by lazy {
        val dir = File(System.getProperty("user.home"), ".runwayiq")
        if (!dir.exists()) dir.mkdirs()
        File(dir, ".keystore")
    }

    private val secretKey: SecretKeySpec by lazy {
        if (!keyFile.exists()) {
            val bytes = ByteArray(KEY_SIZE_BYTES)
            SecureRandom().nextBytes(bytes)
            keyFile.writeBytes(bytes)
        }
        keyFile.setReadable(false, false)
        keyFile.setReadable(true, true)
        keyFile.setWritable(false, false)
        keyFile.setWritable(true, true)
        SecretKeySpec(keyFile.readBytes(), "AES")
    }

    fun encrypt(plainText: String): String {
        if (plainText.isBlank()) return ""
        val iv = ByteArray(IV_SIZE_BYTES).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_BITS, iv))
        val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(iv + cipherText)
    }

    fun decrypt(encoded: String): String {
        if (encoded.isBlank()) return ""
        return try {
            val combined = Base64.getDecoder().decode(encoded)
            val iv = combined.copyOfRange(0, IV_SIZE_BYTES)
            val cipherText = combined.copyOfRange(IV_SIZE_BYTES, combined.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_BITS, iv))
            String(cipher.doFinal(cipherText), Charsets.UTF_8)
        } catch (e: Exception) {
            ""
        }
    }
}
