package com.blankthings.basebackend.user

import com.blankthings.basebackend.email.EmailService
import com.blankthings.basebackend.magiclinktoken.MagicLinkTokenService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(
    private val analytics: Logger = LoggerFactory.getLogger(UserService::class.java),
    private val tokenService: MagicLinkTokenService,
    private val emailService: EmailService,
    private val userRepository: UserRepository
) {
    fun login(email: String) {
        // TODO - problem with re-using the same email. Probably in findByEmail.
        val user = userRepository.findByEmail(email) ?: createNewUser(email)
        analytics.info("login($email) .. user:" + user.email + " , " + user.id + " , " + user.magicLinkToken)
        processLogin(user)
    }

    private fun processLogin(user: User) {
        val token = tokenService.generateToken(user)
        analytics.info("ProcessLogin(): Token: $token")
        emailService.sendAuthEmail(user.email, token)
    }

    private fun createNewUser(email: String): User = userRepository.save(User(email = email))

    fun authenticate(token: String) {
        analytics.info("authenticate token: $token")
        val status = tokenService.validate(token)
        analytics.info("Authenticate result: $status")
    }
}