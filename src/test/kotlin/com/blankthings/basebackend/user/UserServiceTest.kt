package com.blankthings.basebackend.user

import com.blankthings.basebackend.auth.JwtService
import com.blankthings.basebackend.auth.RefreshTokenService
import com.blankthings.basebackend.email.EmailService
import com.blankthings.basebackend.magiclinktoken.MagicLinkTokenService
import com.blankthings.basebackend.magiclinktoken.TokenStatus
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserServiceTest {
    private val magicLinkTokenService = mockk<MagicLinkTokenService>()
    private val emailService = mockk<EmailService>()
    private val jwtService = mockk<JwtService>()
    private val refreshTokenService = mockk<RefreshTokenService>()
    private val userRepository = mockk<UserRepository>()

    private val service =
        UserService(
            magicLinkTokenService,
            emailService,
            jwtService,
            refreshTokenService,
            userRepository,
        )

    private val user = User(id = 1L, email = "user@example.com")

    @BeforeEach
    fun setUp() {
        every { emailService.sendAuthEmail(any(), any()) } just runs
    }

    // --- processLogin ---

    @Test
    fun `processLogin sends auth email when token is new`() {
        every { userRepository.findByEmail(user.email) } returns user
        every { magicLinkTokenService.upsertToken(user) } returns TokenStatus.New("rawtoken")

        service.processLogin(user.email)

        verify(exactly = 1) { emailService.sendAuthEmail(user.email, "rawtoken") }
    }

    @Test
    fun `processLogin does not send email when token already exists`() {
        every { userRepository.findByEmail(user.email) } returns user
        every { magicLinkTokenService.upsertToken(user) } returns TokenStatus.Existing

        service.processLogin(user.email)

        verify(exactly = 0) { emailService.sendAuthEmail(any(), any()) }
    }

    @Test
    fun `processLogin creates a new user when email is not found`() {
        val newUser = User(id = 2L, email = "new@example.com")
        every { userRepository.findByEmail(newUser.email) } returns null
        every { userRepository.save(any()) } returns newUser
        every { magicLinkTokenService.upsertToken(newUser) } returns TokenStatus.Existing

        service.processLogin(newUser.email)

        verify(exactly = 1) { userRepository.save(any()) }
    }

    @Test
    fun `processLogin reuses existing user when email is found`() {
        every { userRepository.findByEmail(user.email) } returns user
        every { magicLinkTokenService.upsertToken(user) } returns TokenStatus.Existing

        service.processLogin(user.email)

        verify(exactly = 0) { userRepository.save(any()) }
    }

    // --- authenticate ---

    @Test
    fun `authenticate returns Success with tokens when magic link token is valid`() {
        every { magicLinkTokenService.validate("rawtoken") } returns user
        every { jwtService.generateAccessToken(user) } returns "access-token"
        every { refreshTokenService.createOrRotateRefreshToken(user) } returns "refresh-token"

        val result = service.authenticate("rawtoken")

        assertTrue(result is Session.Data)
        result as Session.Data
        assertEquals("access-token", result.accessToken)
        assertEquals("refresh-token", result.refreshToken)
    }

    @Test
    fun `authenticate returns Failed when magic link token is invalid`() {
        every { magicLinkTokenService.validate("badtoken") } returns null

        val result = service.authenticate("badtoken")

        assertEquals(Session.None, result)
    }

    // --- refreshSession ---

    @Test
    fun `refreshSession returns Success with new tokens when refresh token is valid`() {
        every { refreshTokenService.validate("rawrefresh") } returns user
        every { jwtService.generateAccessToken(user) } returns "new-access-token"
        every { refreshTokenService.createOrRotateRefreshToken(user) } returns "new-refresh-token"

        val result = service.refreshSession("rawrefresh")

        assertTrue(result is Session.Data)
        result as Session.Data
        assertEquals("new-access-token", result.accessToken)
        assertEquals("new-refresh-token", result.refreshToken)
    }

    @Test
    fun `refreshSession returns Failed when refresh token is invalid`() {
        every { refreshTokenService.validate("badrefresh") } returns null

        val result = service.refreshSession("badrefresh")

        assertEquals(Session.None, result)
    }

    // --- logout ---

    @Test
    fun `logout revokes refresh token when it is valid`() {
        every { refreshTokenService.validate("rawrefresh") } returns user
        every { refreshTokenService.revokeByUserId(user.id) } returns Unit

        service.logout("rawrefresh")

        verify(exactly = 1) { refreshTokenService.revokeByUserId(user.id) }
    }

    @Test
    fun `logout does nothing when refresh token is invalid`() {
        every { refreshTokenService.validate("badrefresh") } returns null

        service.logout("badrefresh")

        verify(exactly = 0) { refreshTokenService.revokeByUserId(any()) }
    }
}
