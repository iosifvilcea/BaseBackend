package com.blankthings.basebackend.profile

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ProfileService(val profileRepository: ProfileRepository) {
    fun findByEmail(email: String): Profile? = profileRepository.findByEmail(email)
    fun findById(id: Long): Profile = profileRepository.findById(id).orElseThrow()
}