package com.blankthings.basebackend.profile

import org.springframework.stereotype.Repository

@Repository
class ProfileRepositoryImpl: ProfileRepository {
    override fun setUsername(username: String) {
        TODO("Not yet implemented - Add to DB")
    }

    override fun setEmail(email: String) {
        TODO("Not yet implemented - Add to DB")
    }

    override fun getProfile(id: Long): Profile {
        TODO("Not yet implemented - Get Profile from DB")
    }
}