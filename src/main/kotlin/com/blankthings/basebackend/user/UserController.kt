package com.blankthings.basebackend.user

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController {

    @GetMapping("/user")
    fun getUser(@RequestParam userId: Long): User = User(id = 1, email = "hello@blankthings.com")

}