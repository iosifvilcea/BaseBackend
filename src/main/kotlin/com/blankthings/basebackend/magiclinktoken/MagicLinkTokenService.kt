package com.blankthings.basebackend.magiclinktoken

import com.blankthings.basebackend.analytics.AnalyticsEvent
import com.blankthings.basebackend.analytics.AnalyticsTracker
import com.blankthings.basebackend.user.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.*
import kotlin.jvm.optionals.getOrNull

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

    // TODO: ERROR
    // if the token is not found or expired, ?.takeIf chain does nothing.
    // should throw an exception
    fun validate(receivedToken: String) {
        val hashed = hashToken(receivedToken)

        magicLinkTokenRepository.findByTokenHash(hashed)
            ?.takeIf {
                it.isValid()
            }
            ?.apply {
                markAsUsed()
                magicLinkTokenRepository.save(this)
                AnalyticsTracker.track(AnalyticsEvent.AUTH_VALIDATED, receivedToken)
            }

        // TODO - GENERATE A JWT TOKEN AND RETURN?
    }

    fun getToken(userId: Long) = magicLinkTokenRepository.findById(userId).getOrNull()
}