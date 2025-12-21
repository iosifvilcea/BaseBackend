package com.blankthings.basebackend.user

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class UserController(private val userRepository: UserRepository) {

    @PostMapping("/users")
    fun create(@RequestParam email: String) = userRepository.createUser(email)

    @GetMapping("/users")
    fun getAll() = userRepository.getUsers()

//    @GetMapping("/users/delete")
//    fun deleteUser(@RequestParam email: String) = userRepository.deleteUser(email)
//
//    @GetMapping("/users/update")
//    fun updateUser(@RequestParam email: String) = userRepository.updateUser(email)
//
//    @GetMapping("/users/{email}")
//    fun getUser(@RequestParam email: String) = userRepository.getUser(email)
//
}