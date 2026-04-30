package com.blankthings.basebackend.profile

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/profile")
class ProfileController(val profileService: ProfileService) {

    @GetMapping("/{email}")
    fun findByEmail(@PathVariable email: String): ResponseEntity<Profile> =
        profileService.findByEmail(email)
            ?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    @GetMapping("/id/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<Profile> =
        ResponseEntity.ok(profileService.findById(id))
}