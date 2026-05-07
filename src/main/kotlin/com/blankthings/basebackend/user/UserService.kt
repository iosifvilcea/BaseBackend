package com.blankthings.basebackend.user

import com.blankthings.basebackend.auth.JwtService
import com.blankthings.basebackend.auth.RefreshTokenService
import com.blankthings.basebackend.email.EmailService
import com.blankthings.basebackend.magiclinktoken.MagicLinkTokenService
import com.blankthings.basebackend.magiclinktoken.TokenStatus
import com.blankthings.basebackend.user.Session.Data
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(
    private val magicLinkTokenService: MagicLinkTokenService,
    private val emailService: EmailService,
    private val jwtService: JwtService,
    private val jwtRefreshTokenService: RefreshTokenService,
    private val userRepository: UserRepository,
) {
    fun processEmail(email: String): Session =
        findOrCreateUser(email).let { user ->
            when (val linkToken = magicLinkTokenService.upsertToken(user)) {
                TokenStatus.Existing -> { /* No-op. */ }

                is TokenStatus.New -> {
                    emailService.sendAuthEmail(user.email, linkToken.token)
                }
            }
            Data() // TODO - I don't like this.
        }

    private fun findOrCreateUser(email: String): User = userRepository.findByEmail(email) ?: createNewUser(email)

    private fun createNewUser(email: String): User = userRepository.save(User(email = email))

    fun authenticate(token: String): Session = magicLinkTokenService.validate(token).let(::issueSession)

    fun refreshSession(rawRefreshToken: String): Session = jwtRefreshTokenService.validate(rawRefreshToken).let(::issueSession)

    private fun issueSession(result: Result): Session =
        when (result) {
            Result.None -> {
                Session.None
            }

            is Result.Data -> {
                Data(
                    accessToken = jwtService.generateAccessToken(result.user),
                    refreshToken = jwtRefreshTokenService.createOrRotateRefreshToken(result.user),
                )
            }
        }

    fun logout(rawRefreshToken: String) =
        when (val data = jwtRefreshTokenService.validate(rawRefreshToken)) {
            is Result.Data -> {
                jwtRefreshTokenService.revokeByUserId(data.user.id)
            }

            Result.None -> { /* No-op */ }
        }
}

sealed class Result {
    data class Data(
        val user: User,
    ) : Result()

    object None : Result()
}

sealed class Session {
    data class Data(
        val accessToken: String = "",
        val refreshToken: String = "",
    ) : Session()

    object None : Session()
}
