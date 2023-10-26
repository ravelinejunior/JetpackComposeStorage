package com.raveline.concord.security

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import com.raveline.concord.R
import com.raveline.concord.util.androidKeyStoreKey
import com.raveline.concord.util.secretStoreKeyAlias
import com.raveline.concord.util.testStoreKeyAlias
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.InvalidKeySpecException
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.security.auth.x500.X500Principal


@RequiresApi(Build.VERSION_CODES.M)
class KeyEncryptDecryptManager {

    // Loads this keystore using the given LoadStoreParameter.
    private val keyStore = KeyStore.getInstance(androidKeyStoreKey).apply {
        load(null)
    }

    //This class provides the functionality of a cryptographic cipher for encryption and decryption.
    //It forms the core of the Java Cryptographic Extension (JCE) framework.
    //Generate the IV param
    private val encryptCipher = Cipher.getInstance(TRANSFORMATION).apply {
        init(Cipher.ENCRYPT_MODE, getKey())
    }


    private fun getDecryptCipherForInitializationVector(iv: ByteArray): Cipher =
        Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, getKey(), IvParameterSpec(iv))
        }

    //A secret (symmetric) key. The purpose of this interface is to group (and provide type safety for) all secret key interfaces
    private fun getKey(): SecretKey {
        val existingKey = keyStore.getEntry(secretStoreKeyAlias, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }

    private fun createKey(): SecretKey =
        KeyGenerator.getInstance(ALGORITHM).apply {
            init(
                KeyGenParameterSpec.Builder(
                    secretStoreKeyAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(BLOCK_MODE)
                    .setEncryptionPaddings(PADDING)
                    .setUserAuthenticationRequired(false)
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )
        }.generateKey()

    fun encrypt(byteArray: ByteArray, outputStream: OutputStream, context: Context): ByteArray {
        val encryptedBytes = encryptCipher.doFinal(byteArray)
        outputStream.use {
            it.write(encryptCipher.iv.size)
            it.write(encryptCipher.iv)
            it.write(encryptedBytes.size)
            it.write(encryptedBytes)
        }

        showKeyInfo()

        generateAndStoreCertificate(context)

        return encryptedBytes
    }

    fun decrypt(inputStream: InputStream, context: Context): ByteArray =
        inputStream.use {
            val ivSize = it.read()
            val iv = ByteArray(ivSize)
            it.read(iv)

            val encryptedBytesSize = it.read()
            val encryptedBytes = ByteArray(encryptedBytesSize)
            it.read(encryptedBytes)

            generateAndStoreCertificate(context)

            showKeyInfo()

            getDecryptCipherForInitializationVector(iv).doFinal(encryptedBytes)
        }

    fun generateAndStoreCertificate(context: Context) {
        try {
            // Generate a new key pair (public-private key)
            val keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA, androidKeyStoreKey
            )

            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                testStoreKeyAlias, KeyProperties.PURPOSE_SIGN
            ).setDigests(KeyProperties.DIGEST_SHA256)
                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                .setCertificateSubject(X500Principal("CN=GTS CA 1C3"))
                .setCertificateNotBefore(Date())
                .setCertificateNotAfter(getFutureDate(365)) // Valid for 1 year
                .build()

            keyPairGenerator.initialize(keyGenParameterSpec)
            val keyPair = keyPairGenerator.genKeyPair()

            // Retrieve the certificate from the Keystore
            val keyStore = KeyStore.getInstance(androidKeyStoreKey)
            keyStore.load(null)

            val certificate = keyStore.getCertificate(testStoreKeyAlias)

            if (certificate != null) {
                val x509Certificate = certificate as X509Certificate
                // You can use x509Certificate for further operations
                println("Generated Certificate:")
                println("Public Key: " + x509Certificate.publicKey.algorithm)
                println("Subject: " + x509Certificate.subjectDN)
                println("Issuer: " + x509Certificate.issuerDN)
                println("Serial Number: " + x509Certificate.serialNumber)
                println("Not Before: " + x509Certificate.notBefore)
                println("Not After: " + x509Certificate.notAfter)
                println("Key pair private: " + keyPair.private.encoded)
                println("Key pair public: " + keyPair.public.encoded)

                println("Certificate generated and stored successfully.")

                showKeyInfo()

                // Usage
                val resourceId = R.raw.mycertificate  // Replace with your certificate's resource ID
                val certificated = loadCertificateFromResource(resourceId, context)

                if (certificated != null) {
                    // Use the certificate here
                    println("Certificate Subject: ${certificate.subjectDN}")
                } else {
                    System.err.println("Failed to load the certificate.")
                }

            } else {
                System.err.println("Failed to generate and store the certificate.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getFutureDate(days: Int): Date {
        val calendar = GregorianCalendar()
        calendar.add(Calendar.DAY_OF_YEAR, days)
        return calendar.time
    }

    private fun loadCertificateFromResource(resourceId: Int, context: Context): X509Certificate? {
        var certificate: X509Certificate? = null
        try {
            val certificateFactory = CertificateFactory.getInstance("X.509")
            val inputStream: InputStream = context.resources.openRawResource(resourceId)
            certificate = certificateFactory.generateCertificate(inputStream) as X509Certificate
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return certificate
    }

    private fun showKeyInfo() {
        val factory = SecretKeyFactory.getInstance(getKey().algorithm, androidKeyStoreKey)
        val keyInfo: KeyInfo
        try {
            keyInfo = factory.getKeySpec(getKey(), KeyInfo::class.java) as KeyInfo
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                println("KeyInfo security level: ${keyInfo.securityLevel}")
            } else {
                println("KeyInfo security level under 29: ${keyInfo.isInsideSecureHardware}")
            }
        } catch (e: InvalidKeySpecException) {
            // Not an Android KeyStore key.
            System.err.println(" Not an Android KeyStore key.")

        }
    }

    companion object {
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
    }
}