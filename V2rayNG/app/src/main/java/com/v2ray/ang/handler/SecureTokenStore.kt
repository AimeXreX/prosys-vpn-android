package com.v2ray.ang.handler

import android.content.Context
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties

/**
 * Keeps the backend device token encrypted at rest with a non-exportable
 * Android Keystore key. The encrypted blob alone is useless off-device.
 */
object SecureTokenStore {
    private const val PREFS = "prosys_secure_tokens"
    private const val VALUE_KEY = "free_device_token_v1"
    private const val KEY_ALIAS = "prosys_free_device_aes_v1"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    private fun secretKey(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
            generateKey()
        }
    }

    fun put(context: Context, token: String) {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, secretKey())
        }
        val encrypted = cipher.doFinal(token.toByteArray(Charsets.UTF_8))
        val packed = ByteArray(1 + cipher.iv.size + encrypted.size).also {
            it[0] = cipher.iv.size.toByte()
            cipher.iv.copyInto(it, 1)
            encrypted.copyInto(it, 1 + cipher.iv.size)
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(VALUE_KEY, Base64.encodeToString(packed, Base64.NO_WRAP))
            .apply()
    }

    fun get(context: Context): String? = runCatching {
        val encoded = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(VALUE_KEY, null)
            ?: return null
        val packed = Base64.decode(encoded, Base64.NO_WRAP)
        val ivSize = packed.first().toInt()
        require(ivSize in 12..32 && packed.size > ivSize + 1)
        val iv = packed.copyOfRange(1, 1 + ivSize)
        val encrypted = packed.copyOfRange(1 + ivSize, packed.size)
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(128, iv))
        }
        String(cipher.doFinal(encrypted), Charsets.UTF_8)
    }.getOrNull()
}
