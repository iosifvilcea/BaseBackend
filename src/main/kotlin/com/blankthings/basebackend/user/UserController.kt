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
        userService.login(loginRequest.email)
        return ResponseEntity.ok(LoginResponse())
    }

    @GetMapping
    fun authenticate(@RequestParam token: String) = userService.authenticate(token)

}

data class LoginRequest(val email: String)

data class LoginResponse(
    val successMessage: String =
        """
            Your login link has been sent!
            Please check your email to login.
            If you can't find the login link in your email, be sure to check your spam folder.
        """.trimIndent()
)