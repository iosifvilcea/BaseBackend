package com.blankthings.basebackend.profile

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/profile")
class ProfileController(val profileRepository: ProfileRepository) {

    @PostMapping
    fun setEmail(@RequestParam email: String) = profileRepository.setEmail(email)

    @PostMapping
    fun setUsername(@RequestParam username: String) = profileRepository.setUsername(username)

    @GetMapping("/profile/{id}")
    fun getProfile(@RequestParam id: Long) = profileRepository.getProfile(id)

}