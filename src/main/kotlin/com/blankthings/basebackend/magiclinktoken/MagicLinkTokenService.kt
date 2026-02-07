package com.blankthings.basebackend.magiclinktoken

import com.blankthings.basebackend.user.User
import org.springframework.http.ResponseEntity.ok
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.Base64

@Service
@Transactional
class MagicLinkTokenService(private val magicLinkTokenRepository: MagicLinkTokenRepository) {

    private val secureRandom = SecureRandom()

    fun generateToken(user: User): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)

        val token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        magicLinkTokenRepository.save(MagicLinkToken(
            user = user,
            tokenHash = token,
            createdAt = LocalDateTime.now(),
            expiresAt = LocalDateTime.now().plusMinutes(15),
            used = false
        ))

        return hashToken(token)
    }

    private fun hashToken(token: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(token.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    fun validate(receivedToken: String): Status {
        val hashToken = hashToken(receivedToken)
        magicLinkTokenRepository.findByTokenHash(hashToken)?.let { token ->
            if (!token.isValid()) {
                return Status.AUTH_FAILURE
            }

            token.markAsUsed()
            magicLinkTokenRepository.save(token)
            return Status.AUTH_SUCCESS
        }

        return Status.AUTH_FAILURE
    }
}

enum class Status {
    AUTH_SUCCESS,
    AUTH_FAILURE
}