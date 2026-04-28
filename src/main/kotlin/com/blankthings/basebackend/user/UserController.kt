package com.blankthings.basebackend.user

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class UserController(private val userService: UserService) {

    @PostMapping
    @ResponseBody
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<LoginResponse> {
        return when (userService.processEmail(loginRequest.email)) {
            AuthResult.Failed -> ResponseEntity.ok(LoginResponse.Failed)
            is AuthResult.Success -> ResponseEntity.ok(LoginResponse.Success())
        }
    }

    @GetMapping
    fun authenticate(@RequestParam token: String): ResponseEntity<AuthResponse> {
        return when(val auth = userService.authenticate(token)) {
            is AuthResult.Success -> ResponseEntity.ok(AuthResponse(jwt = auth.jwt))
            AuthResult.Failed -> ResponseEntity.notFound().build()
        }
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
    ): LoginResponse()
    object Failed : LoginResponse()
}

data class AuthResponse(val jwt: String, val successMessage: String = "Login successful!")