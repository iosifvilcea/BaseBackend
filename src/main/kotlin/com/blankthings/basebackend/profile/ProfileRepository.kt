package com.blankthings.basebackend.profile

interface ProfileRepository {
    fun setUsername(username: String)
    fun setEmail(email: String)
    fun getProfile(id: Long): Profile
}