package com.blankthings.basebackend.magiclinktoken

import com.blankthings.basebackend.auth.Session
import com.blankthings.basebackend.user.User
import com.blankthings.basebackend.utils.Utils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
@Transactional
class MagicLinkTokenService(
    private val magicLinkTokenRepository: MagicLinkTokenRepository
) {

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
        val rawToken = Utils.generateSecureToken()
        val refreshedToken = token.copy(
            tokenHash = Utils.hashToken(rawToken),
            expiresAt = Instant.now().plusSeconds(EXPIRATION_TIME_OF_15_MINUTES_IN_SECONDS),
            createdAt = Instant.now(),
            used = false
        )
        magicLinkTokenRepository.save(refreshedToken)
        return TokenStatus.New(rawToken)
    }

    fun validate(receivedToken: String): Session {
        val hashed = Utils.hashToken(receivedToken)
        return magicLinkTokenRepository.findByTokenHash(hashed)
            ?.takeIf {
                it.isValid()
            }?.apply {
                markAsUsed()
                magicLinkTokenRepository.save(this)
            }?.let {
                Session.Data(user = it.user)
            } ?: Session.None
    }
}

sealed class TokenStatus {
    data class New(val token: String): TokenStatus()
    object Existing: TokenStatus()
}