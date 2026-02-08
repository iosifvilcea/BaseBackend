package com.blankthings.basebackend.magiclinktoken

import com.blankthings.basebackend.user.User
import com.blankthings.basebackend.user.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity.ok
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.Base64

@Service
@Transactional
class MagicLinkTokenService(
    private val analytics: Logger = LoggerFactory.getLogger(MagicLinkTokenService::class.java),
    private val magicLinkTokenRepository: MagicLinkTokenRepository
) {

    private val secureRandom = SecureRandom()

    fun generateToken(user: User): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)

        val token = hashToken(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes))
        magicLinkTokenRepository.save(MagicLinkToken(
            user = user,
            tokenHash = token,
            createdAt = LocalDateTime.now(),
            expiresAt = LocalDateTime.now().plusMinutes(15),
            used = false
        ))

        return token
    }

    private fun hashToken(token: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(token.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    // TODO - make this functional.
    fun validate(receivedToken: String) {
        analytics.info("validate: receivedToken: $receivedToken")
        magicLinkTokenRepository.findByTokenHash(receivedToken)?.let { token ->
            if (!token.isValid()) {
                analytics.info("validate: token is not valid.")
                return
            }

            token.markAsUsed()
            magicLinkTokenRepository.save(token)
            analytics.info("validate: Token works.")
            return
        }

        analytics.info("validate: token not found.")
    }
}