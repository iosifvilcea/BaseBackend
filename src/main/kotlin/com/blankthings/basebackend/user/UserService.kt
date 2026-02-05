package com.blankthings.basebackend.user

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(private val userRepository: UserRepository) {
    fun login(email: String) {
        val result = userRepository.findByEmail(email)?.let {
            processUser(it)
        } ?: createNewUser(email)
    }

    private fun processUser(user: User): Status {
        return Status.USER_LOGIN_SUCCESS
    }

    private fun createNewUser(email: String): Status {
        return Status.USER_CREATE_SUCCESS
    }
}

enum class Status {
    USER_LOGIN_SUCCESS,
    USER_LOGIN_FAILURE,
    USER_CREATE_SUCCESS,
    USER_CREATE_FAILURE
}