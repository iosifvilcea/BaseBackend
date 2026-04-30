package com.blankthings.basebackend.user

import com.blankthings.basebackend.analytics.AnalyticsEvent
import com.blankthings.basebackend.analytics.AnalyticsTracker
import com.blankthings.basebackend.email.EmailService
import com.blankthings.basebackend.magiclinktoken.MagicLinkTokenService
import com.blankthings.basebackend.magiclinktoken.TokenStatus
import com.blankthings.basebackend.user.AuthResult.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(
    private val tokenService: MagicLinkTokenService,
    private val emailService: EmailService,
    private val userRepository: UserRepository
) {
    fun processEmail(email: String): AuthResult {
        val user = findOrCreateUser(email)
        AnalyticsTracker.track(AnalyticsEvent.DEBUG, "UserService: processEmail(), generating token next ..")
        return when (val linkToken = tokenService.upsertToken(user)) {
            TokenStatus.Existing -> Success()
            is TokenStatus.New -> {
                emailService.sendAuthEmail(user.email, linkToken.token)
                Success()
            }
        }
    }

    fun findOrCreateUser(email: String): User {
        return userRepository.findByEmail(email).also {
            AnalyticsTracker.track(AnalyticsEvent.DEBUG, "login(): FOUND USER:${it?.email}")
        } ?: createNewUser(email).also {
            AnalyticsTracker.track(AnalyticsEvent.DEBUG, "login(): CREATED USER:${it.email}")
        }
    }

    private fun createNewUser(email: String): User = userRepository.save(User(email = email))

    fun authenticate(token: String): AuthResult {
        return tokenService.validate(token)
            .takeIf { it is Success }
            ?.let { Success(generateJwt()) }
            ?: Failed
    }

    private fun generateJwt(): String {
        return "abc123"
    }
}

sealed class AuthResult {
    data class Success(val jwt: String = "") : AuthResult()
    object Failed : AuthResult()
}