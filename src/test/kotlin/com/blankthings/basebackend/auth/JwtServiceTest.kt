package com.blankthings.basebackend.auth

import com.blankthings.basebackend.user.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class JwtServiceTest {

    // 32-byte key (256-bit) — minimum required for HMAC-SHA256
    private val testSecret = "YWJjZGVmZ2hpamtsbW5vcHFyc3R1dnd4eXoxMjM0NTY="
    private val oneHourMs = 3_600_000L

    private val jwtService = JwtService(secret = testSecret, expirationMs = oneHourMs)

    private val testUser = User(id = 42L, email = "test@example.com")

    @Test
    fun `generateAccessToken returns a non-blank string`() {
        val token = jwtService.generateAccessToken(testUser)
        assertTrue(token.isNotBlank())
    }

    @Test
    fun `generateAccessToken returns a three-part JWT string`() {
        val token = jwtService.generateAccessToken(testUser)
        assertEquals(3, token.split(".").size)
    }

    @Test
    fun `validateToken returns the user ID for a freshly generated token`() {
        val token = jwtService.generateAccessToken(testUser)
        val userId = jwtService.validateToken(token)
        assertEquals(testUser.id, userId)
    }

    @Test
    fun `validateToken returns null for an expired token`() {
        val expiredJwtService = JwtService(secret = testSecret, expirationMs = -3_600_000L)
        val token = expiredJwtService.generateAccessToken(testUser)
        assertNull(jwtService.validateToken(token))
    }

    @Test
    fun `validateToken returns null for a tampered token`() {
        val token = jwtService.generateAccessToken(testUser)
        val tampered = token.dropLast(5) + "XXXXX"
        assertNull(jwtService.validateToken(tampered))
    }

    @Test
    fun `validateToken returns null for a random garbage string`() {
        assertNull(jwtService.validateToken("not.a.jwt"))
    }
}
