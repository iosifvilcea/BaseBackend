package com.blankthings.basebackend.auth

import com.blankthings.basebackend.user.SessionResult
import com.blankthings.basebackend.user.User
import com.blankthings.basebackend.utils.Utils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
@Transactional
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository,
    @Value("\${jwt.refresh-token-expiration-days}") private val expirationDays: Long
) {

    fun createOrRotateRefreshToken(user: User): String {
        val rawToken = Utils.generateSecureToken()
        val token = findOrCreateRefreshToken(
            user,
            Utils.hashToken(rawToken),
            Instant.now().plusSeconds(expirationDays * 24 * 3600)
        )
        refreshTokenRepository.save(token)
        return rawToken
    }

    private fun findOrCreateRefreshToken(user: User, hashToken: String, expiresAt: Instant) =
        refreshTokenRepository.findByUserId(user.id)?.apply {
            this.tokenHash = hashToken
            this.expiresAt = expiresAt
        } ?: RefreshToken(
            user = user,
            tokenHash = hashToken,
            expiresAt = expiresAt
        )

    fun validate(rawToken: String): SessionResult =
        Utils.hashToken(rawToken)
            .let { refreshTokenRepository.findByTokenHash(it) }
            ?.takeIf { !it.isExpired() }
            ?.let { SessionResult.Data(user = it.user) }
            ?: SessionResult.None

    fun revokeByUserId(userId: Long) = refreshTokenRepository.deleteByUserId(userId)
}
