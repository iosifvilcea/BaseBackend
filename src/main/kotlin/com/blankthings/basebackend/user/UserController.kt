package com.blankthings.basebackend.user

import com.blankthings.basebackend.auth.CookieManager
import com.blankthings.basebackend.auth.REFRESH_TOKEN
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

const val AUTH_URL_PATH = "/api/auth"

@RestController
@RequestMapping(AUTH_URL_PATH)
class UserController(
    private val userService: UserService,
    private val cookieManager: CookieManager
) {
    @PostMapping
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<LoginResponse> =
        when (userService.processEmail(loginRequest.email)) {
            AuthResult.Failed -> ResponseEntity.ok(LoginResponse.Failed)
            is AuthResult.Success -> ResponseEntity.ok(LoginResponse.Success())
        }

    @GetMapping
    fun authenticate(@RequestParam token: String): ResponseEntity<AuthResponse> =
        buildAuthResponse(userService.authenticate(token))

    @PostMapping("/refresh")
    fun refresh(@CookieValue(REFRESH_TOKEN, required = false) rawRefreshToken: String?): ResponseEntity<AuthResponse> =
        when (rawRefreshToken) {
            null -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
            else -> buildAuthResponse(userService.refreshSession(rawRefreshToken))
        }

    private fun buildAuthResponse(result: AuthResult): ResponseEntity<AuthResponse> =
        when (result) {
            AuthResult.Failed -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
            is AuthResult.Success -> ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieManager.accessCookie(result.accessToken).toString())
                .header(HttpHeaders.SET_COOKIE, cookieManager.refreshCookie(result.refreshToken).toString())
                .body(AuthResponse)
        }
}

data class LoginRequest(val email: String)

sealed class LoginResponse {
    data class Success(
        val successMessage: String =
            """
            Your login link has been sent!
            Please check your email to login.
            If you can't find the login link in your email, be sure to check your spam folder.
        """.trimIndent()
    ) : LoginResponse()
    object Failed : LoginResponse()
}

// TODO - This should carry on? Go to HOME?
object AuthResponse
