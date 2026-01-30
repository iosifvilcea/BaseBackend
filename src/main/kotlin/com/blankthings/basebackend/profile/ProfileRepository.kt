package com.blankthings.basebackend.profile

import org.springframework.data.jpa.repository.JpaRepository

interface ProfileRepository: JpaRepository<Profile, Long> {
    fun findByEmail(email: String): Profile?
}