package com.blankthings.basebackend.magiclinktoken

import org.springframework.data.jpa.repository.JpaRepository

interface MagicLinkTokenRepository: JpaRepository<MagicLinkToken, Long> {
    fun findByTokenHash(token: String): MagicLinkToken?
}