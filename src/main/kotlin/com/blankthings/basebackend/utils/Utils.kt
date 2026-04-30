package com.blankthings.basebackend.utils

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object Utils {
    private val secureRandom = SecureRandom()

    fun generateSecureToken(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    fun hashToken(token: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(token.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}