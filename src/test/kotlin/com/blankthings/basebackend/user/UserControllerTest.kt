package com.blankthings.basebackend.user

import com.blankthings.basebackend.auth.ACCESS_TOKEN
import com.blankthings.basebackend.auth.CookieManager
import com.blankthings.basebackend.auth.JwtService
import com.blankthings.basebackend.auth.REFRESH_TOKEN
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseCookie
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest

@WebMvcTest(UserController::class)
class UserControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var userService: UserService

    @MockitoBean
    private lateinit var cookieManager: CookieManager

    @MockitoBean
    private lateinit var jwtService: JwtService

    @MockitoBean
    private lateinit var userRepository: UserRepository

    private fun stubAuthCookies() {
        given(cookieManager.accessCookie(any())).willReturn(
            ResponseCookie.from(ACCESS_TOKEN, "access-token").build()
        )
        given(cookieManager.refreshCookie(any())).willReturn(
            ResponseCookie.from(REFRESH_TOKEN, "refresh-token").build()
        )
    }

    // --- POST /api/auth ---

    @Test
    fun `POST login returns 200 with success message when email is processed`() {
        given(userService.processEmail(any())).willReturn(AuthResult.Success())

        mockMvc.post("/api/auth") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email": "user@example.com"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.successMessage") { exists() }
        }
    }

    @Test
    fun `POST login returns 200 when processEmail returns Failed`() {
        given(userService.processEmail(any())).willReturn(AuthResult.Failed)

        mockMvc.post("/api/auth") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email": "user@example.com"}"""
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `POST login returns 400 when request body is missing`() {
        mockMvc.post("/api/auth") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isBadRequest() }
        }
    }

    // --- GET /api/auth?token=... ---

    @Test
    fun `GET authenticate returns 200 and sets cookies when token is valid`() {
        given(userService.authenticate("validtoken")).willReturn(AuthResult.Success("access", "refresh"))
        stubAuthCookies()

        mockMvc.get("/api/auth") {
            param("token", "validtoken")
        }.andExpect {
            status { isOk() }
            header { exists(HttpHeaders.SET_COOKIE) }
        }
    }

    @Test
    fun `GET authenticate returns 401 when token is invalid`() {
        given(userService.authenticate("badtoken")).willReturn(AuthResult.Failed)

        mockMvc.get("/api/auth") {
            param("token", "badtoken")
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `GET authenticate returns 400 when token param is missing`() {
        mockMvc.get("/api/auth").andExpect {
            status { isBadRequest() }
        }
    }

    // --- POST /api/auth/refresh ---

    @Test
    fun `POST refresh returns 401 when refresh cookie is absent`() {
        mockMvc.post("/api/auth/refresh").andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `POST refresh returns 200 and sets cookies when refresh token is valid`() {
        given(userService.refreshSession("rawrefresh")).willReturn(AuthResult.Success("access", "refresh"))
        stubAuthCookies()

        mockMvc.post("/api/auth/refresh") {
            cookie(Cookie(REFRESH_TOKEN, "rawrefresh"))
        }.andExpect {
            status { isOk() }
            header { exists(HttpHeaders.SET_COOKIE) }
        }
    }

    @Test
    fun `POST refresh returns 401 when refresh token is invalid`() {
        given(userService.refreshSession("badrefresh")).willReturn(AuthResult.Failed)

        mockMvc.post("/api/auth/refresh") {
            cookie(Cookie(REFRESH_TOKEN, "badrefresh"))
        }.andExpect {
            status { isUnauthorized() }
        }
    }
}
