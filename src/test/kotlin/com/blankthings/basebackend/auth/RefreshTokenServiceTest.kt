package com.blankthings.basebackend.auth

import com.blankthings.basebackend.user.SessionResult
import com.blankthings.basebackend.user.User
import com.blankthings.basebackend.utils.Utils
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class RefreshTokenServiceTest {

    private val repo = mockk<RefreshTokenRepository>()
    private val expirationDays = 30L
    private val service = RefreshTokenService(repo, expirationDays)

    private val user = User(id = 1L, email = "user@example.com")

    @BeforeEach
    fun setUp() {
        every { repo.save(any()) } answers { firstArg() }
    }

    // --- createOrRotateRefreshToken ---

    @Test
    fun `createOrRotateRefreshToken returns a non-blank raw token`() {
        every { repo.findByUserId(user.id) } returns null

        val rawToken = service.createOrRotateRefreshToken(user)

        assertTrue(rawToken.isNotBlank())
    }

    @Test
    fun `createOrRotateRefreshToken saves only the hash, not the raw token`() {
        every { repo.findByUserId(user.id) } returns null
        val savedSlot = slot<RefreshToken>()
        every { repo.save(capture(savedSlot)) } answers { firstArg() }

        val rawToken = service.createOrRotateRefreshToken(user)

        assertNotEquals(rawToken, savedSlot.captured.tokenHash)
        assertEquals(Utils.hashToken(rawToken), savedSlot.captured.tokenHash)
    }

    @Test
    fun `createOrRotateRefreshToken sets expiry to expirationDays from now`() {
        every { repo.findByUserId(user.id) } returns null
        val savedSlot = slot<RefreshToken>()
        every { repo.save(capture(savedSlot)) } answers { firstArg() }

        service.createOrRotateRefreshToken(user)

        val expiresAt = savedSlot.captured.expiresAt
        val expectedExpiry = Instant.now().plusSeconds(expirationDays * 24 * 3600)
        assertTrue(expiresAt.isAfter(expectedExpiry.minusSeconds(5)))
        assertTrue(expiresAt.isBefore(expectedExpiry.plusSeconds(5)))
    }

    @Test
    fun `createOrRotateRefreshToken rotates existing token instead of creating a new one`() {
        val existingToken = RefreshToken(
            user = user,
            tokenHash = "oldhash",
            expiresAt = Instant.now().plusSeconds(3600)
        )
        every { repo.findByUserId(user.id) } returns existingToken
        val savedSlot = slot<RefreshToken>()
        every { repo.save(capture(savedSlot)) } answers { firstArg() }

        val rawToken = service.createOrRotateRefreshToken(user)

        val saved = savedSlot.captured
        assertSame(existingToken, saved)
        assertNotEquals("oldhash", saved.tokenHash)
        assertEquals(Utils.hashToken(rawToken), saved.tokenHash)
    }

    @Test
    fun `createOrRotateRefreshToken always calls save once`() {
        every { repo.findByUserId(user.id) } returns null

        service.createOrRotateRefreshToken(user)

        verify(exactly = 1) { repo.save(any()) }
    }

    // --- validate ---

    @Test
    fun `validate returns None when token hash is not found`() {
        every { repo.findByTokenHash(any()) } returns null

        val result = service.validate("rawtoken")

        assertEquals(SessionResult.None, result)
    }

    @Test
    fun `validate returns None when token is expired`() {
        val expiredToken = RefreshToken(
            user = user,
            tokenHash = Utils.hashToken("rawtoken"),
            expiresAt = Instant.now().minusSeconds(1)
        )
        every { repo.findByTokenHash(any()) } returns expiredToken

        val result = service.validate("rawtoken")

        assertEquals(SessionResult.None, result)
    }

    @Test
    fun `validate returns SessionResult Data with user for a valid token`() {
        val validToken = RefreshToken(
            user = user,
            tokenHash = Utils.hashToken("rawtoken"),
            expiresAt = Instant.now().plusSeconds(3600)
        )
        every { repo.findByTokenHash(any()) } returns validToken

        val result = service.validate("rawtoken")

        assertTrue(result is SessionResult.Data)
        assertEquals(user, (result as SessionResult.Data).user)
    }

    @Test
    fun `validate looks up by hash of the raw token, not the raw token itself`() {
        val rawToken = "rawtoken"
        every { repo.findByTokenHash(any()) } returns null

        service.validate(rawToken)

        verify { repo.findByTokenHash(Utils.hashToken(rawToken)) }
        verify(exactly = 0) { repo.findByTokenHash(rawToken) }
    }

    // --- revokeByUserId ---

    @Test
    fun `revokeByUserId delegates to repository deleteByUserId`() {
        every { repo.deleteByUserId(user.id) } returns Unit

        service.revokeByUserId(user.id)

        verify(exactly = 1) { repo.deleteByUserId(user.id) }
    }
}
