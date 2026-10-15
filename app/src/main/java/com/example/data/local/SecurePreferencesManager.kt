package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecurePreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("omnia_max_secure_prefs", Context.MODE_PRIVATE)
    private val keyAlias = "OmniaMaxKeystoreKey"

    init {
        initKeystoreKey()
    }

    private fun initKeystoreKey() {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            if (!keyStore.containsAlias(keyAlias)) {
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    keyAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()
            }
        } catch (e: Exception) {
            // Fallback gracefully if keystore init has device constraints
        }
    }

    private fun getSecretKey(): SecretKey? {
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            keyStore.getKey(keyAlias, null) as? SecretKey
        } catch (e: Exception) {
            null
        }
    }

    fun saveEncryptedKey(keyName: String, plainValue: String) {
        if (plainValue.isBlank()) {
            prefs.edit().remove(keyName).apply()
            return
        }

        val secretKey = getSecretKey()
        if (secretKey == null) {
            // Fallback obfuscated storage if Keystore unavailable
            val encoded = Base64.encodeToString(plainValue.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            prefs.edit().putString(keyName, "OBF:$encoded").apply()
            return
        }

        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(plainValue.toByteArray(Charsets.UTF_8))

            val combined = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)

            val base64 = Base64.encodeToString(combined, Base64.NO_WRAP)
            prefs.edit().putString(keyName, "ENC:$base64").apply()
        } catch (e: Exception) {
            val encoded = Base64.encodeToString(plainValue.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            prefs.edit().putString(keyName, "OBF:$encoded").apply()
        }
    }

    fun getDecryptedKey(keyName: String, defaultValue: String = ""): String {
        val stored = prefs.getString(keyName, null) ?: return defaultValue
        if (stored.isBlank()) return defaultValue

        if (stored.startsWith("OBF:")) {
            return try {
                val decoded = Base64.decode(stored.removePrefix("OBF:"), Base64.NO_WRAP)
                String(decoded, Charsets.UTF_8)
            } catch (e: Exception) {
                defaultValue
            }
        }

        if (stored.startsWith("ENC:")) {
            val secretKey = getSecretKey() ?: return defaultValue
            return try {
                val combined = Base64.decode(stored.removePrefix("ENC:"), Base64.NO_WRAP)
                val iv = ByteArray(12) // GCM IV size
                val encryptedBytes = ByteArray(combined.size - 12)

                System.arraycopy(combined, 0, iv, 0, 12)
                System.arraycopy(combined, 12, encryptedBytes, 0, encryptedBytes.size)

                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                val spec = GCMParameterSpec(128, iv)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

                val decryptedBytes = cipher.doFinal(encryptedBytes)
                String(decryptedBytes, Charsets.UTF_8)
            } catch (e: Exception) {
                defaultValue
            }
        }

        return stored
    }
}
