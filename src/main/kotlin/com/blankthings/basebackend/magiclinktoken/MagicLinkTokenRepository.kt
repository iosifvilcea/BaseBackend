package com.blankthings.basebackend.magiclinktoken

import com.blankthings.basebackend.profile.Profile
import org.springframework.data.jpa.repository.JpaRepository

interface MagicLinkTokenRepository: JpaRepository<MagicLinkToken, Long>