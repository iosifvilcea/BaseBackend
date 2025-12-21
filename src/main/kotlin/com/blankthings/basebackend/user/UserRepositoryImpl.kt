package com.blankthings.basebackend.user

import com.blankthings.basebackend.analytics.AnalyticsEvent
import com.blankthings.basebackend.analytics.AnalyticsTracker
import org.springframework.stereotype.Repository
import kotlin.random.Random

// TODO - Add error handling.
val NONE = User(id = 0, email = "N/A")

@Repository
class UserRepositoryImpl(val analyticsTracker: AnalyticsTracker = AnalyticsTracker()): UserRepository {
    private val users = mutableMapOf<String, User>()

    override fun createUser(email: String): User {
        val createdUser = User(id = Random.nextLong(), email = email)
        users[createdUser.email] = createdUser
        analyticsTracker.track(AnalyticsEvent.USER_CREATED, createdUser)
        return createdUser
    }

    override fun getUser(email: String): User {
        val userFound = users[email] ?: NONE
        analyticsTracker.track(AnalyticsEvent.USER_FETCHED, userFound)
        return userFound
    }

    override fun getUsers(): List<User> {
        val retrievedUsers = users.values.toList()
        analyticsTracker.track(AnalyticsEvent.USERS_FETCHED, retrievedUsers)
        return retrievedUsers
    }
}