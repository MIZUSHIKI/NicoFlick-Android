package com.mizushiki.nicoflick_a

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


object Crypt {
    fun getKeyAndGenerateIv(
        password: String,
        salt: ByteArray,
        key_bytes: ByteArray,
        iv_bytes: ByteArray
    ): Boolean {
        try {
            val password_bytes: ByteArray = password.toByteArray(StandardCharsets.UTF_8)
            var length = password_bytes.size + salt.size
            var byte_buffer: ByteBuffer = ByteBuffer.allocate(length)
            byte_buffer.put(password_bytes)
            byte_buffer.put(salt)
            byte_buffer.rewind()
            var byte_array = ByteArray(length)
            byte_buffer.get(byte_array)
            System.arraycopy(
                MessageDigest.getInstance("MD5").digest(byte_array),
                0,
                key_bytes,
                0,
                key_bytes.size
            )
            length = password_bytes.size + salt.size + key_bytes.size
            byte_buffer = ByteBuffer.allocate(length)
            byte_buffer.put(key_bytes)
            byte_buffer.put(password_bytes)
            byte_buffer.put(salt)
            byte_buffer.rewind()
            byte_array = ByteArray(length)
            byte_buffer.get(byte_array)
            System.arraycopy(
                MessageDigest.getInstance("MD5").digest(byte_array),
                0,
                iv_bytes,
                0,
                iv_bytes.size
            )
        } catch (e: NoSuchAlgorithmException) {
            return false
        }
        return true
    }

    fun getKeyAndGenerateIv(
        password: String,
        salt_text: String,
        key_bytes: ByteArray,
        iv_bytes: ByteArray
    ): Boolean {
        try {
            val password_bytes: ByteArray = password.toByteArray(StandardCharsets.UTF_8)
            val salt = salt_text.toByteArray(StandardCharsets.UTF_8) //ByteArray(8)
            var length = password_bytes.size + salt.size
            var byte_buffer: ByteBuffer = ByteBuffer.allocate(length)
            byte_buffer.put(password_bytes)
            byte_buffer.put(salt)
            byte_buffer.rewind()
            var byte_array = ByteArray(length)
            byte_buffer.get(byte_array)
            System.arraycopy(
                MessageDigest.getInstance("MD5").digest(byte_array),
                0,
                key_bytes,
                0,
                key_bytes.size
            )
            /*
            length = password_bytes.size + salt.size + key_bytes.size
            byte_buffer = ByteBuffer.allocate(length)
            byte_buffer.put(key_bytes)
            byte_buffer.put(password_bytes)
            byte_buffer.put(salt)
            byte_buffer.rewind()
            byte_array = ByteArray(length)
            byte_buffer.get(byte_array)
             */
            val iv_text = key_bytes.toHexString() + password + salt_text
            System.arraycopy(
                MessageDigest.getInstance("MD5").digest(iv_text.toByteArray()),
                0,
                iv_bytes,
                0,
                iv_bytes.size
            )
        } catch (e: NoSuchAlgorithmException) {
            return false
        }
        return true
    }

    fun encrypt(plaintext: String, password: String): String { // Generate random salt.
        val random_bytes = ByteArray(8)
        SecureRandom().nextBytes(random_bytes)
        val key_bytes = ByteArray(16)
        val iv_bytes = ByteArray(16)
        getKeyAndGenerateIv(password, random_bytes, key_bytes, iv_bytes)
        val secret: SecretKey = SecretKeySpec(key_bytes, "AES")
        val ivspec = IvParameterSpec(iv_bytes)
        val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secret, ivspec)
        val encrypted_bytes: ByteArray = cipher.doFinal(plaintext.toByteArray(StandardCharsets.UTF_8))
        val header_string = "Salted__"
        val header_bytes: ByteArray = header_string.toByteArray(StandardCharsets.UTF_8)
        val length = header_bytes.size + random_bytes.size + encrypted_bytes.size
        val byte_buffer: ByteBuffer = ByteBuffer.allocate(length)
        byte_buffer.put(header_bytes)
        byte_buffer.put(random_bytes)
        byte_buffer.put(encrypted_bytes)
        byte_buffer.rewind()
        val byte_array = ByteArray(length)
        byte_buffer.get(byte_array)
        return String(Base64.getEncoder().encodeToString(byte_array))
    }
    fun encryptx_urlsafe(plaintext: String, password: String): String { //NicoFlick仕様
        val salt = UUID.randomUUID().toString().substring(0..7)
        val random_bytes = salt.toByteArray(StandardCharsets.UTF_8) //ByteArray(8)
        //SecureRandom().nextBytes(random_bytes)
        val key_bytes = ByteArray(16)
        val iv_bytes = ByteArray(16)
        getKeyAndGenerateIv(password=password, salt_text=salt, key_bytes = key_bytes, iv_bytes = iv_bytes)
        val secret: SecretKey = SecretKeySpec(key_bytes, "AES")
        val ivspec = IvParameterSpec(iv_bytes)
        val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secret, ivspec)
        val encrypted_bytes: ByteArray = cipher.doFinal(plaintext.toByteArray(StandardCharsets.UTF_8))
        val header_string = "Saltedx_"
        val header_bytes: ByteArray = header_string.toByteArray(StandardCharsets.UTF_8)
        val length = header_bytes.size + random_bytes.size + encrypted_bytes.size
        val byte_buffer: ByteBuffer = ByteBuffer.allocate(length)
        byte_buffer.put(header_bytes)
        byte_buffer.put(random_bytes)
        byte_buffer.put(encrypted_bytes)
        byte_buffer.rewind()
        val byte_array = ByteArray(length)
        byte_buffer.get(byte_array)
        return String(Base64.getEncoder().encodeToString(byte_array)).replace("+","_").replace("/","-").replace("=",".")
    }

    fun decrypt(payload: String, password: String): String {
        val payload_bytes: ByteArray = Base64.getDecoder().decode(payload.toByteArray(StandardCharsets.UTF_8))
        val header_bytes = ByteArray(8)
        val salt_bytes = ByteArray(8)
        var length = payload_bytes.size
        val byte_buffer: ByteBuffer = ByteBuffer.allocate(length)
        byte_buffer.put(payload_bytes)
        byte_buffer.rewind()
        byte_buffer.get(header_bytes)
        byte_buffer.get(salt_bytes)
        length = payload_bytes.size - header_bytes.size - salt_bytes.size
        val data_bytes = ByteArray(length)
        byte_buffer.get(data_bytes)
        val key_byte = ByteArray(16)
        val iv_bytes = ByteArray(16)
        getKeyAndGenerateIv(password, salt_bytes, key_byte, iv_bytes)
        val secret: SecretKey = SecretKeySpec(key_byte, "AES")
        val ivspec = IvParameterSpec(iv_bytes)
        val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secret, ivspec)
        val decrypted: ByteArray = cipher.doFinal(data_bytes)
        return String(decrypted)
    }
/*
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) { // 暗号化対象データ・パスワードを読み込み
        val br = BufferedReader(InputStreamReader(System.`in`))
        print("Plain text: ")
        System.out.flush()
        val plain_text: String = br.readLine()
        print("Password: ")
        System.out.flush()
        val password: String = br.readLine()
        // 暗号化処理
        val encrypted: String = EncryptorDecryptor.encrypt(plain_text, password)
        print("encrypted：$encrypted")
        println()
        // 復号処理
        val decrypted: String = EncryptorDecryptor.decrypt(encrypted, password)
        print("decrypted：$decrypted")
        println()
    }
 */
}

fun ByteArray.toHexString() : String {
    return this.joinToString("") { "%02x".format(it) }
}
