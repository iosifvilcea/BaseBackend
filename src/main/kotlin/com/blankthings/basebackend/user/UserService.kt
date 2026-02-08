package com.blankthings.basebackend.user

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
    private val userRepository: UserRepository
) {
    fun login(email: String) {
        val user = userRepository.findByEmail(email) ?: createNewUser(email)
        analytics.info("login($email) .. user:" + user.email + " , " + user.id + " , " + user.magicLinkToken)
        processLogin(user)
    }

    private fun processLogin(user: User) {
        val token = tokenService.generateToken(user)
        analytics.info("ProcessLogin(): Token: $token")
        // TODO - Generate EMAIL
        // TODO - SEND EMAIL
        // TODO - UPDATE UI THAT EMAIL HAS BEEN SENT
    }

    private fun createNewUser(email: String): User = userRepository.save(User(email = email))

    fun authenticate(token: String) {
        tokenService.validate(token)
    }
}