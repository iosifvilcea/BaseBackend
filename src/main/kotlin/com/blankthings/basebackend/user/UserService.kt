package com.blankthings.basebackend.user

import com.blankthings.basebackend.auth.JwtService
import com.blankthings.basebackend.auth.RefreshTokenService
import com.blankthings.basebackend.email.EmailService
import com.blankthings.basebackend.magiclinktoken.MagicLinkTokenService
import com.blankthings.basebackend.magiclinktoken.TokenStatus
import com.blankthings.basebackend.user.AuthResult.Failed
import com.blankthings.basebackend.user.AuthResult.Success
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(
    private val magicLinkTokenService: MagicLinkTokenService,
    private val emailService: EmailService,
    private val jwtService: JwtService,
    private val jwtRefreshTokenService: RefreshTokenService,
    private val userRepository: UserRepository
) {
    fun processEmail(email: String): AuthResult =
        findOrCreateUser(email).let { user ->
            when (val linkToken = magicLinkTokenService.upsertToken(user)) {
                TokenStatus.Existing -> Success()
                is TokenStatus.New -> {
                    emailService.sendAuthEmail(user.email, linkToken.token)
                    Success()
                }
            }
        }

    fun findOrCreateUser(email: String): User = userRepository.findByEmail(email) ?: createNewUser(email)

    private fun createNewUser(email: String): User = userRepository.save(User(email = email))

    fun authenticate(token: String): AuthResult =
        magicLinkTokenService.validate(token).let(::mapToAuthResult)

    fun refreshSession(rawRefreshToken: String): AuthResult =
        jwtRefreshTokenService.validate(rawRefreshToken).let(::mapToAuthResult)

    private fun mapToAuthResult(sessionResult: SessionResult): AuthResult =
        when (sessionResult) {
            SessionResult.None -> Failed
            is SessionResult.Data -> Success(
                accessToken = jwtService.generateAccessToken(sessionResult.user),
                refreshToken = jwtRefreshTokenService.createOrRotateRefreshToken(sessionResult.user)
            )
        }

    fun logout(rawRefreshToken: String) =
        when (val data = jwtRefreshTokenService.validate(rawRefreshToken)) {
            is SessionResult.Data -> jwtRefreshTokenService.revokeByUserId(data.user.id)
            SessionResult.None -> { /** No-op */ }
        }
}

sealed class SessionResult {
    data class Data(val user: User) : SessionResult()
    object None : SessionResult()
}

sealed class AuthResult {
    data class Success(val accessToken: String = "", val refreshToken: String = "") : AuthResult()
    object Failed : AuthResult()
}
