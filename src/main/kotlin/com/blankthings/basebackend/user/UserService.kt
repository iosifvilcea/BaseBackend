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
    fun processEmail(email: String): AuthResult {
        val user = findOrCreateUser(email)
        return when (val linkToken = magicLinkTokenService.upsertToken(user)) {
            TokenStatus.Existing -> Success()
            is TokenStatus.New -> {
                emailService.sendAuthEmail(user.email, linkToken.token)
                Success()
            }
        }
    }

    fun findOrCreateUser(email: String): User {
        return userRepository.findByEmail(email) ?: createNewUser(email)
    }

    private fun createNewUser(email: String): User = userRepository.save(User(email = email))

    fun authenticate(token: String): AuthResult {
        return mapToAuthResult(magicLinkTokenService.validate(token))
    }

    fun refreshSession(rawRefreshToken: String): AuthResult {
        return mapToAuthResult(jwtRefreshTokenService.validate(rawRefreshToken))
    }

    private fun mapToAuthResult(session: Session): AuthResult {
        return when (session) {
            Session.None -> Failed
            is Session.Data -> Success(
                accessToken = jwtService.generateAccessToken(session.user),
                refreshToken = jwtRefreshTokenService.createOrRotate(session.user)
            )
        }
    }

    fun logout(rawRefreshToken: String) {
        when (val data = jwtRefreshTokenService.validate(rawRefreshToken)) {
            is Session.Data -> jwtRefreshTokenService.revokeByUserId(data.user.id)
            Session.None -> { /** No-op */ }
        }
    }
}

sealed class Session {
    data class Data(val user: User) : Session()
    object None : Session()
}

sealed class AuthResult {
    data class Success(val accessToken: String = "", val refreshToken: String = "") : AuthResult()
    object Failed : AuthResult()
}
