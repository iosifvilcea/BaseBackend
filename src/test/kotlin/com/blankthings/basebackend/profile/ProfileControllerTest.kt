package com.blankthings.basebackend.profile

import com.blankthings.basebackend.auth.CookieManager
import com.blankthings.basebackend.auth.JwtService
import com.blankthings.basebackend.user.UserRepository
import com.blankthings.basebackend.user.UserService
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(ProfileController::class)
class ProfileControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var profileService: ProfileService

    @MockitoBean
    private lateinit var userService: UserService

    @MockitoBean
    private lateinit var cookieManager: CookieManager

    @MockitoBean
    private lateinit var jwtService: JwtService

    @MockitoBean
    private lateinit var userRepository: UserRepository

    private val profile = Profile(id = 1L, email = "user@example.com")

    // --- GET /api/profile/{email} ---

    @Test
    @WithMockUser
    fun `GET findByEmail returns 200 with profile when found`() {
        given(profileService.findByEmail(profile.email)).willReturn(profile)

        mockMvc.get("/api/profile/${profile.email}").andExpect {
            status { isOk() }
            jsonPath("$.id") { value(profile.id) }
            jsonPath("$.email") { value(profile.email) }
        }
    }

    @Test
    @WithMockUser
    fun `GET findByEmail returns 404 when not found`() {
        given(profileService.findByEmail("unknown@example.com")).willReturn(null)

        mockMvc.get("/api/profile/unknown@example.com").andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `GET findByEmail returns 401 when not authenticated`() {
        mockMvc.get("/api/profile/${profile.email}").andExpect {
            status { isUnauthorized() }
        }
    }

    // --- GET /api/profile/id/{id} ---

    @Test
    @WithMockUser
    fun `GET findById returns 200 with profile when found`() {
        given(profileService.findById(profile.id)).willReturn(profile)

        mockMvc.get("/api/profile/id/${profile.id}").andExpect {
            status { isOk() }
            jsonPath("$.id") { value(profile.id) }
            jsonPath("$.email") { value(profile.email) }
        }
    }

    @Test
    @WithMockUser
    fun `GET findById returns 500 when not found`() {
        given(profileService.findById(99L)).willThrow(NoSuchElementException("not found"))

        mockMvc.get("/api/profile/id/99").andExpect {
            status { isInternalServerError() }
        }
    }

    @Test
    fun `GET findById returns 401 when not authenticated`() {
        mockMvc.get("/api/profile/id/${profile.id}").andExpect {
            status { isUnauthorized() }
        }
    }
}
