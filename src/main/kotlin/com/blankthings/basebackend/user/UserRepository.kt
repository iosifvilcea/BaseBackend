package com.blankthings.basebackend.user

import com.blankthings.basebackend.profile.Profile
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository: JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
}