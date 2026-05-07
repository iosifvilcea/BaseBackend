package com.blankthings.basebackend.profile

class ProfileNotFoundException(
    id: Long,
) : RuntimeException("Profile not found: $id")
