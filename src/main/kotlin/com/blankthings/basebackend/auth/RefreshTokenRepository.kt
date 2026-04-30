package com.blankthings.basebackend.auth

import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun findByUserId(userId: Long): RefreshToken?
    fun findByTokenHash(tokenHash: String): RefreshToken?
    fun deleteByUserId(userId: Long)
}
