package com.blankthings.basebackend.user

class UserNotFoundException(
    email: String,
) : RuntimeException("User not found: $email")
