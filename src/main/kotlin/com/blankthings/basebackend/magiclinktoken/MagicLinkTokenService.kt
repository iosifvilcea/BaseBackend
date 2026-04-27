package com.blankthings.basebackend.magiclinktoken

import com.blankthings.basebackend.analytics.AnalyticsEvent
import com.blankthings.basebackend.analytics.AnalyticsTracker
import com.blankthings.basebackend.user.AuthResult
import com.blankthings.basebackend.user.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class MagicLinkTokenService(
    private val magicLinkTokenRepository: MagicLinkTokenRepository
) {

    private val secureRandom = SecureRandom()

    fun generateToken(user: User): String {
        val token = generateSecureToken()
        val hashToken = hashToken(token)

        val existing = magicLinkTokenRepository.findByUserId(user.id!!)
        val entity = existing?.copy(
            user = user,
            tokenHash = hashToken,
            createdAt = LocalDateTime.now(),
            expiresAt = LocalDateTime.now().plusMinutes(15),
            used = false,
        ) ?: MagicLinkToken(
                user = user,
                tokenHash = hashToken,
                createdAt = LocalDateTime.now(),
                expiresAt = LocalDateTime.now().plusMinutes(15),
                used = false
            )

        magicLinkTokenRepository.save(entity)
        return token
    }

    fun generateSecureToken(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun hashToken(token: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(token.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }


    fun validate(receivedToken: String): AuthResult {
        val hashed = hashToken(receivedToken)
        return magicLinkTokenRepository.findByTokenHash(hashed)
            ?.takeIf {
                it.isValid()
            }?.apply {
                AnalyticsTracker.track(AnalyticsEvent.DEBUG, "WHATWHAT:" + isValid())
                markAsUsed()
                magicLinkTokenRepository.save(this)
            }?.let {
                AuthResult.Success("")
            } ?: AuthResult.Failed
    }
}