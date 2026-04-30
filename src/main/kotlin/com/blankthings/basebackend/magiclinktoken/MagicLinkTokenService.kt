package com.blankthings.basebackend.magiclinktoken

import com.blankthings.basebackend.user.AuthResult
import com.blankthings.basebackend.user.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.util.*

@Service
@Transactional
class MagicLinkTokenService(
    private val magicLinkTokenRepository: MagicLinkTokenRepository
) {

    private val secureRandom = SecureRandom()

    fun upsertToken(user: User): TokenStatus {
        return magicLinkTokenRepository.findByUserId(user.id)
            ?.let(::updateCurrentToken)
            ?: refreshToken(MagicLinkToken(user = user, tokenHash = ""))
    }

    private fun updateCurrentToken(token: MagicLinkToken): TokenStatus {
        return if (!token.isValid()) {
            refreshToken(token)
        } else {
            TokenStatus.Existing
        }
    }

    private fun refreshToken(token: MagicLinkToken): TokenStatus {
        val rawToken = generateSecureToken()
        val refreshedToken = token.copy(
            tokenHash = hashToken(rawToken),
            expiresAt = Instant.now().plusSeconds(EXPIRATION_TIME_OF_15_MINUTES_IN_SECONDS),
            createdAt = Instant.now(),
            used = false
        )
        magicLinkTokenRepository.save(refreshedToken)
        return TokenStatus.New(rawToken)
    }

    private fun generateSecureToken(): String {
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
                markAsUsed()
                magicLinkTokenRepository.save(this)
            }?.let {
                AuthResult.Success()
            } ?: AuthResult.Failed
    }
}

sealed class TokenStatus {
    data class New(val token: String): TokenStatus()
    object Existing: TokenStatus()
}