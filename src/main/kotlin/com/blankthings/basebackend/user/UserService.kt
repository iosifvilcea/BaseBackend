package com.blankthings.basebackend.user

import com.blankthings.basebackend.analytics.AnalyticsTracker
import com.blankthings.basebackend.email.EmailService
import com.blankthings.basebackend.magiclinktoken.MagicLinkTokenService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(
    private val tokenService: MagicLinkTokenService,
    private val emailService: EmailService,
    private val userRepository: UserRepository
) {
    fun login(email: String) {
        val user = userRepository.findByEmail(email) ?: createNewUser(email)
        processLogin(user)
    }

    private fun processLogin(user: User) {
        val generatedToken = tokenService.generateToken(user)
        emailService.sendAuthEmail(user.email, generatedToken)
    }

    private fun createNewUser(email: String): User = userRepository.save(User(email = email))

    fun authenticate(token: String) {
        val status = tokenService.validate(token)
    }
}