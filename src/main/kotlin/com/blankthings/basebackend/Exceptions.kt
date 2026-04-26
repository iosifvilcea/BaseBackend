package com.blankthings.basebackend

class UserNotFoundException(email: String) : RuntimeException("User not found: $email")
