package com.blankthings.basebackend.user

import com.blankthings.basebackend.auth.AUTH_URL_PATH
import com.blankthings.basebackend.auth.CookieManager
import com.blankthings.basebackend.auth.REFRESH_TOKEN
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(AUTH_URL_PATH)
class UserController(
    private val userService: UserService,
    private val cookieManager: CookieManager,
) {
    @PostMapping
    fun login(
        @RequestBody @Valid loginRequest: LoginRequest,
    ): ResponseEntity<LoginResponse> =
        when (userService.processEmail(loginRequest.email)) {
            Session.None -> ResponseEntity.ok(LoginResponse.Failed)
            is Session.Data -> ResponseEntity.ok(LoginResponse.Success())
        }

    @GetMapping
    fun authenticate(
        @RequestParam token: String,
    ): ResponseEntity<AuthResponse> = buildAuthResponse(userService.authenticate(token))

    @PostMapping("/refresh")
    fun refresh(
        @CookieValue(REFRESH_TOKEN, required = false) rawRefreshToken: String?,
    ): ResponseEntity<AuthResponse> =
        when (rawRefreshToken) {
            null -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
            else -> buildAuthResponse(userService.refreshSession(rawRefreshToken))
        }

    private fun buildAuthResponse(result: Session): ResponseEntity<AuthResponse> =
        when (result) {
            Session.None -> {
                ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
            }

            is Session.Data -> {
                ResponseEntity
                    .ok()
                    .header(HttpHeaders.SET_COOKIE, cookieManager.accessCookie(result.accessToken).toString())
                    .header(HttpHeaders.SET_COOKIE, cookieManager.refreshCookie(result.refreshToken).toString())
                    .body(AuthResponse)
            }
        }
}

data class LoginRequest(
    @field:Email @field:NotBlank val email: String,
)

sealed class LoginResponse {
    data class Success(
        val successMessage: String =
            """
            Your login link has been sent!
            Please check your email to login.
            If you can't find the login link in your email, be sure to check your spam folder.
            """.trimIndent(),
    ) : LoginResponse()

    object Failed : LoginResponse()
}

// TODO - This should carry on? Go to HOME?
object AuthResponse
