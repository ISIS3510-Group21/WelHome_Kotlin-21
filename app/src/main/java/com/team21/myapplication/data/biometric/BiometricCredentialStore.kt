package com.team21.myapplication.data.biometric

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

/**
 * Aquí creo y uso una clave AES en AndroidKeyStore que exige biometría para (de)crifrar.
 * Guardo email/password cifrados en EncryptedSharedPreferences.
 */
class BiometricCredentialStore(private val context: Context) {

    private val prefsName = "bio_login_prefs"
    private val keyEmail = "enc_email"
    private val keyPass  = "enc_pass"
    private val keyIv    = "enc_iv"
    private val keyAlias = "bio_login_aes_key" // alias en Keystore
    private val keyBlob = "enc_blob"

    fun isBiometricAvailable(): Boolean {
        val bm = BiometricManager.from(context)
        return bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /** Aquí genero/recupero la clave simétrica que exige biometría. */
    private fun getOrCreateSecretKey(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(keyAlias, null) as? SecretKey)?.let { return it }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            error("Android M+ required for biometric-protected keys")

        val keyGenerator = KeyGenerator.getInstance("AES", "AndroidKeyStore")
        val builder = android.security.keystore.KeyGenParameterSpec.Builder(
            keyAlias,
            android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or
                    android.security.keystore.KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setUserAuthenticationRequired(true) // exige autenticación del usuario

        // Aquí hago compatibilidad por versión:
        // - API 30+: biometría fuerte por uso
        // - API 24–29: requerir auth en cada uso con validez -1
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            builder.setUserAuthenticationParameters(
                /* timeoutSeconds = */ 0,
                android.security.keystore.KeyProperties.AUTH_BIOMETRIC_STRONG
            )
        } else {
            @Suppress("DEPRECATION")
            builder.setUserAuthenticationValidityDurationSeconds(-1)
        }

        val spec = builder.build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    /** Aquí creo el Cipher en modo ENCRYPT. */
    fun createEncryptCipher(): Cipher =
        Cipher.getInstance("AES/CBC/PKCS7Padding").apply {
            init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        }

    /** Aquí creo el Cipher en modo DECRYPT usando el IV guardado. */
    fun createDecryptCipher(iv: ByteArray): Cipher =
        Cipher.getInstance("AES/CBC/PKCS7Padding").apply {
            init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), IvParameterSpec(iv))
        }

    private fun prefs() = EncryptedSharedPreferences.create(
        context,
        prefsName,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun clear() {
        prefs().edit().remove(keyEmail).remove(keyPass).remove(keyIv).apply()
    }

    fun hasLinkedFingerprint(): Boolean {
        val p = prefs()
        return p.contains(keyBlob) && p.contains(keyIv)
    }

    /** Aquí cifro y persisto email/password con la huella (clave Keystore). */
    fun saveEncryptedCredentials(cipher: Cipher, email: String, password: String) {
        // Usamos un separador que no va a aparecer en email/password (NULL)
        val payload = (email + '\u0000' + password).toByteArray(Charsets.UTF_8)
        val enc = cipher.doFinal(payload) // <- un solo doFinal
        val iv  = cipher.iv

        prefs().edit()
            .putString(keyBlob, enc.encodeB64())
            .putString(keyIv,   iv.encodeB64())
            .apply()
    }

    fun getDecryptCipherOrNull(): Cipher? {
        val ivB64 = prefs().getString(keyIv, null) ?: return null
        val iv = ivB64.decodeB64() ?: return null
        return createDecryptCipher(iv)
    }

    /** Aquí desencripto email y password después de autenticar con huella. */
    /** Aquí desencripto y separo en email y password. */
    fun decryptEmailAndPassword(cipher: Cipher): Pair<String, String> {
        val p = prefs()
        val enc = p.getString(keyBlob, null)!!.decodeB64()!!
        val plainBytes = cipher.doFinal(enc)
        val plain = String(plainBytes, Charsets.UTF_8)

        val sep = plain.indexOf('\u0000')
        if (sep <= 0) error("Corrupted payload")

        val email = plain.substring(0, sep)
        val pass  = plain.substring(sep + 1)
        return email to pass
    }

}

private fun ByteArray.encodeB64(): String =
    android.util.Base64.encodeToString(this, android.util.Base64.NO_WRAP)

private fun String.decodeB64(): ByteArray? =
    try { android.util.Base64.decode(this, android.util.Base64.NO_WRAP) } catch (_: Exception) { null }

