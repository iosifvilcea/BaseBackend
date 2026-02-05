package com.blankthings.basebackend.user

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @PostMapping
    fun login(@RequestParam email: String) = userService.login(email)

//    @PostMapping
//    fun create(@RequestParam email: String) = userService.createUser(email)
//
//    @GetMapping("{email}")
//    fun getUser(@RequestParam email: String) = userService.getUser(email)

//    @GetMapping
//    fun getAll() = ...
}