package com.blankthings.basebackend.user

import com.blankthings.basebackend.auth.JwtService
import com.blankthings.basebackend.auth.RefreshTokenService
import com.blankthings.basebackend.email.EmailService
import com.blankthings.basebackend.magiclinktoken.MagicLinkTokenService
import com.blankthings.basebackend.magiclinktoken.TokenStatus
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
    fun processLogin(email: String) {
        val user = findOrCreateUser(email)
        when (val linkToken = magicLinkTokenService.upsertToken(user)) {
            TokenStatus.Existing -> { /* No-op. */ }

            is TokenStatus.New -> {
                emailService.sendAuthEmail(user.email, linkToken.token)
            }
        }
    }

    private fun findOrCreateUser(email: String): User = userRepository.findByEmail(email) ?: createNewUser(email)

    private fun createNewUser(email: String): User = userRepository.save(User(email = email))

    fun authenticate(token: String): Session = magicLinkTokenService.validate(token).let(::issueSession)

    fun refreshSession(rawRefreshToken: String): Session = jwtRefreshTokenService.validate(rawRefreshToken).let(::issueSession)

    private fun issueSession(user: User?): Session =
        user?.let {
            Session.Data(
                accessToken = jwtService.generateAccessToken(it),
                refreshToken = jwtRefreshTokenService.createOrRotateRefreshToken(it),
            )
        } ?: Session.None

    fun logout(rawRefreshToken: String) {
        jwtRefreshTokenService
            .validate(rawRefreshToken)
            ?.let { jwtRefreshTokenService.revokeByUserId(it.id) }
    }
}

sealed class Session {
    data class Data(
        val accessToken: String = "",
        val refreshToken: String = "",
    ) : Session()

    object None : Session()
}
