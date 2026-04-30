package com.blankthings.basebackend.auth

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

    fun createOrRotate(user: User): String {
        val expiresAt = Instant.now().plusSeconds(expirationDays * 24 * 3600)
        val rawToken = Utils.generateSecureToken()
        val token = refreshTokenRepository.findByUserId(user.id)?.copy(
            tokenHash = Utils.hashToken(rawToken),
            expiresAt = expiresAt
        ) ?: RefreshToken(
            user = user,
            tokenHash = Utils.hashToken(rawToken),
            expiresAt = expiresAt
        )
        refreshTokenRepository.save(token)
        return rawToken
    }

    fun validate(rawToken: String): Session =
        refreshTokenRepository.findByTokenHash(Utils.hashToken(rawToken))
            ?.takeIf { !it.isExpired() }
            ?.let { Session.Data(user = it.user) }
            ?: Session.None

    fun revokeByUserId(userId: Long) = refreshTokenRepository.deleteByUserId(userId)
}

sealed class Session {
    data class Data(val user: User) : Session()
    object None : Session()
}
