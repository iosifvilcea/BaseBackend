package com.blankthings.basebackend.profile

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/profile")
class ProfileController(val profileService: ProfileService) {

//    @PostMapping("/email")
//    fun setEmail(@RequestParam email: String) = profileService.setEmail(email)

//    @PostMapping("/username")
//    fun setUsername(@RequestParam username: String) = profileRepository.setUsername(username)

    @GetMapping("/{email}")
    fun findByEmail(@PathVariable email: String) = profileService.findByEmail(email)

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): Profile = profileService.findById(id)
}