package com.blankthings.basebackend.auth

import com.blankthings.basebackend.user.Session
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
        val expiresAt = Instant.now().plusSeconds(expirationDays * 24 * 3600)
        val rawToken = Utils.generateSecureToken()
        val hashToken = Utils.hashToken(rawToken)
        val token = refreshTokenRepository.findByUserId(user.id)?.apply {
            this.tokenHash = hashToken
            this.expiresAt = expiresAt
        } ?: RefreshToken(
            user = user,
            tokenHash = hashToken,
            expiresAt = expiresAt
        )
        refreshTokenRepository.save(token)
        return rawToken
    }

    fun validate(rawToken: String): Session =
        Utils.hashToken(rawToken)
            .let { refreshTokenRepository.findByTokenHash(it) }
            ?.takeIf { !it.isExpired() }
            ?.let { Session.Data(user = it.user) }
            ?: Session.None

    fun revokeByUserId(userId: Long) = refreshTokenRepository.deleteByUserId(userId)
}
