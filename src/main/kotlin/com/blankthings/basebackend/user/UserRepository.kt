package com.blankthings.basebackend.user

interface UserRepository {
    fun createUser(email: String): User
//    fun updateUser(email: String): User
//    fun deleteUser(email: String): Boolean
    fun getUser(email: String): User
    fun getUsers(): List<User>
}