package com.blankthings.basebackend.user

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
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
    fun login(@RequestBody loginDao: LoginDao): ResponseEntity<LoginResponse> {
        userService.login(loginDao.email)
        return ResponseEntity.ok(LoginResponse())
    }

    @GetMapping("{token}")
    fun authenticate(@RequestParam token: String) = userService.authenticate(token)

}

data class LoginDao(val email: String)

data class LoginResponse(val successMessage: String = "Your login link has been sent!")