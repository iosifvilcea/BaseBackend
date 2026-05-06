package com.blankthings.basebackend.magiclinktoken

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

class MagicLinkTokenServiceTest {

    private val repo = mockk<MagicLinkTokenRepository>()
    private val service = MagicLinkTokenService(repo)

    private val user = User(id = 1L, email = "user@example.com")

    @BeforeEach
    fun setUp() {
        every { repo.save(any()) } answers { firstArg() }
    }

    // --- upsertToken ---

    @Test
    fun `upsertToken returns New when no existing token`() {
        every { repo.findByUserId(user.id) } returns null

        val result = service.upsertToken(user)

        assertTrue(result is TokenStatus.New)
    }

    @Test
    fun `upsertToken saves only the hash, not the raw token`() {
        every { repo.findByUserId(user.id) } returns null
        val savedSlot = slot<MagicLinkToken>()
        every { repo.save(capture(savedSlot)) } answers { firstArg() }

        val result = service.upsertToken(user) as TokenStatus.New

        val saved = savedSlot.captured
        assertNotEquals(result.token, saved.tokenHash)
        assertEquals(Utils.hashToken(result.token), saved.tokenHash)
    }

    @Test
    fun `upsertToken sets expiry roughly 15 minutes in the future`() {
        every { repo.findByUserId(user.id) } returns null
        val savedSlot = slot<MagicLinkToken>()
        every { repo.save(capture(savedSlot)) } answers { firstArg() }

        service.upsertToken(user)

        val expiresAt = savedSlot.captured.expiresAt
        val expectedExpiry = Instant.now().plusSeconds(EXPIRATION_TIME_15_MINS_IN_SECONDS)
        assertTrue(expiresAt.isAfter(expectedExpiry.minusSeconds(5)))
        assertTrue(expiresAt.isBefore(expectedExpiry.plusSeconds(5)))
    }

    @Test
    fun `upsertToken returns Existing when a valid token already exists`() {
        val validToken = MagicLinkToken(
            user = user,
            tokenHash = "somehash",
            expiresAt = Instant.now().plusSeconds(900)
        )
        every { repo.findByUserId(user.id) } returns validToken

        val result = service.upsertToken(user)

        assertEquals(TokenStatus.Existing, result)
    }

    @Test
    fun `upsertToken does not save when a valid token already exists`() {
        val validToken = MagicLinkToken(
            user = user,
            tokenHash = "somehash",
            expiresAt = Instant.now().plusSeconds(900)
        )
        every { repo.findByUserId(user.id) } returns validToken

        service.upsertToken(user)

        verify(exactly = 0) { repo.save(any()) }
    }

    @Test
    fun `upsertToken refreshes and returns New when existing token is expired`() {
        val expiredToken = MagicLinkToken(
            user = user,
            tokenHash = "oldhash",
            expiresAt = Instant.now().minusSeconds(1)
        )
        every { repo.findByUserId(user.id) } returns expiredToken

        val result = service.upsertToken(user)

        assertTrue(result is TokenStatus.New)
        verify(exactly = 1) { repo.save(any()) }
    }

    @Test
    fun `upsertToken refreshes and returns New when existing token is already used`() {
        val usedToken = MagicLinkToken(
            user = user,
            tokenHash = "usedhash",
            expiresAt = Instant.now().plusSeconds(900),
            used = true
        )
        every { repo.findByUserId(user.id) } returns usedToken

        val result = service.upsertToken(user)

        assertTrue(result is TokenStatus.New)
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
        val expiredToken = MagicLinkToken(
            user = user,
            tokenHash = Utils.hashToken("rawtoken"),
            expiresAt = Instant.now().minusSeconds(1)
        )
        every { repo.findByTokenHash(any()) } returns expiredToken

        val result = service.validate("rawtoken")

        assertEquals(SessionResult.None, result)
    }

    @Test
    fun `validate returns None when token is already used`() {
        val usedToken = MagicLinkToken(
            user = user,
            tokenHash = Utils.hashToken("rawtoken"),
            expiresAt = Instant.now().plusSeconds(900),
            used = true
        )
        every { repo.findByTokenHash(any()) } returns usedToken

        val result = service.validate("rawtoken")

        assertEquals(SessionResult.None, result)
    }

    @Test
    fun `validate returns SessionResult Data with user when token is valid`() {
        val validToken = MagicLinkToken(
            user = user,
            tokenHash = Utils.hashToken("rawtoken"),
            expiresAt = Instant.now().plusSeconds(900)
        )
        every { repo.findByTokenHash(any()) } returns validToken

        val result = service.validate("rawtoken")

        assertTrue(result is SessionResult.Data)
        assertEquals(user, (result as SessionResult.Data).user)
    }

    @Test
    fun `validate marks the token as used and saves it`() {
        val validToken = MagicLinkToken(
            user = user,
            tokenHash = Utils.hashToken("rawtoken"),
            expiresAt = Instant.now().plusSeconds(900)
        )
        every { repo.findByTokenHash(any()) } returns validToken

        service.validate("rawtoken")

        assertFalse(validToken.isValid())
        verify(exactly = 1) { repo.save(validToken) }
    }

    @Test
    fun `validate looks up by hash of the raw token, not the raw token itself`() {
        val rawToken = "rawtoken"
        every { repo.findByTokenHash(any()) } returns null

        service.validate(rawToken)

        verify { repo.findByTokenHash(Utils.hashToken(rawToken)) }
        verify(exactly = 0) { repo.findByTokenHash(rawToken) }
    }
}
