package com.blankthings.basebackend.user

import org.springframework.stereotype.Repository
import kotlin.random.Random

@Repository
class UserRepositoryImpl: UserRepository {
    private val users = mutableMapOf<String, User>()

    override fun createUser(email: String): User {
        val createdUser = User(id = Random.nextLong(), email = email)
        users[createdUser.email] = createdUser
        return createdUser
    }

    override fun getUsers(): List<User> = users.values.toList()
}