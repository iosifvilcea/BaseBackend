package com.blankthings.basebackend.profile

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.Optional

class ProfileServiceTest {

    private val repo = mockk<ProfileRepository>()
    private val service = ProfileService(repo)

    private val profile = Profile(id = 1L, email = "user@example.com")

    // --- findByEmail ---

    @Test
    fun `findByEmail returns profile when found`() {
        every { repo.findByEmail(profile.email) } returns profile

        val result = service.findByEmail(profile.email)

        assertEquals(profile, result)
    }

    @Test
    fun `findByEmail returns null when not found`() {
        every { repo.findByEmail(any()) } returns null

        val result = service.findByEmail("unknown@example.com")

        assertNull(result)
    }

    // --- findById ---

    @Test
    fun `findById returns profile when found`() {
        every { repo.findById(profile.id) } returns Optional.of(profile)

        val result = service.findById(profile.id)

        assertEquals(profile, result)
    }

    @Test
    fun `findById throws UserNotFoundException when not found`() {
        every { repo.findById(any()) } returns Optional.empty()

        assertThrows(com.blankthings.basebackend.UserNotFoundException::class.java) {
            service.findById(99L)
        }
    }
}
