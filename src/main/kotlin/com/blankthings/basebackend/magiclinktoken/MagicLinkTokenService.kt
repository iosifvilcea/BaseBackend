package com.blankthings.basebackend.magiclinktoken

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class MagicLinkTokenService(private val magicLinkTokenRepository: MagicLinkTokenRepository) {
    fun generate(email: String) {
        // TODO - GENERATE TOKEN W/ TIMESTAMP & EXPIRATION
        // TODO - BUILD LINK
        // TODO - SEND LINK
        // TODO - UPDATE UI WITH SUCCESSFUL SEND CONFIRMATION.
    }

    fun validate(link: String) {
        // TODO - VALIDATE:
        // - SEE IF IT EXISTS IN DB.
        // - IF IT EXISTS, VALIDATE IF LINK IS STILL VALID (Has it existed more than 15 minutes?)
        // - LOGIN IN USER / SAVE SESSION TOKEN
    }
}