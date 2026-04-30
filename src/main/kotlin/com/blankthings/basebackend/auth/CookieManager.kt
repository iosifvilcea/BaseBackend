package com.blankthings.basebackend.auth

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseCookie
import java.time.Duration

const val REFRESH_TOKEN = "refresh_token"
const val ACCESS_TOKEN = "access_token"

class CookieManager(
    @Value("\${jwt.expiration-ms}") private val jwtExpirationMs: Long,
    @Value("\${jwt.refresh-token-expiration-days}") private val refreshExpirationDays: Long,
    @Value("\${app.cookie.secure}") private val cookieSecure: Boolean
) {
    fun accessCookie(token: String): ResponseCookie =
        ResponseCookie.from(ACCESS_TOKEN, token)
            .httpOnly(true)
            .secure(cookieSecure)
            .path("/")
            .maxAge(Duration.ofMillis(jwtExpirationMs))
            .sameSite("Strict")
            .build()

    fun refreshCookie(token: String): ResponseCookie =
        ResponseCookie.from(REFRESH_TOKEN, token)
            .httpOnly(true)
            .secure(cookieSecure)
            .path("/api/auth")
            .maxAge(Duration.ofDays(refreshExpirationDays))
            .sameSite("Strict")
            .build()

    fun clearCookie(name: String, path: String): ResponseCookie =
        ResponseCookie.from(name, "")
            .httpOnly(true)
            .path(path)
            .maxAge(0)
            .build()
}