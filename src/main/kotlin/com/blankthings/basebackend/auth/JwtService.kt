package com.blankthings.basebackend.auth

import com.blankthings.basebackend.analytics.AnalyticsEvent
import com.blankthings.basebackend.analytics.AnalyticsTracker
import com.blankthings.basebackend.user.User
import com.blankthings.basebackend.user.toUserDto
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expiration-ms}") private val expirationMs: Long
) {
    fun generateAccessToken(user: User): String =
        Jwts.builder()
            .subject(user.id.toString())
            .claim("email", user.email)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expirationMs))
            .signWith(signingKey())
            .compact()

    fun validateToken(token: String): Long? =
        try {
            Jwts.parser()
                .verifyWith(signingKey())
                .clockSkewSeconds(60)
                .build()
                .parseSignedClaims(token)
                .payload
                .subject
                .toLongOrNull()
        } catch (e: JwtException) {
            AnalyticsTracker.track(AnalyticsEvent.AUTH_ERROR, "Error parsing JWT Token.", e)
            null
        }

    private fun signingKey(): SecretKey =
        Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))
}
