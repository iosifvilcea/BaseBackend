package com.blankthings.basebackend.user

import com.blankthings.basebackend.email.EmailService
import com.blankthings.basebackend.magiclinktoken.MagicLinkTokenService
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

    fun authenticate(token: String): AuthResult {
        return tokenService.validate(token)
            .takeIf { it is AuthResult.Success }
            ?.let {
                val jwt = generateJwt()
                AuthResult.Success(jwt)
            } ?: AuthResult.Failed
    }

    private fun generateJwt(): String {
        return "abc123"
    }
}

sealed class AuthResult {
    data class Success(val jwt: String) : AuthResult()
    object Failed : AuthResult()
}