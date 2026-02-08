package com.blankthings.basebackend.email

import com.blankthings.basebackend.user.UserService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

const val EMAIL_FROM = "no-reply@blankthings.com"
const val EMAIL_SUBJECT = "Login link for blankthings.com"
const val EMAIL_MESSAGE = "Here's your login for blankthings.com:\n\nwww.blankthings.com/auth/token="

private val analytics: Logger = LoggerFactory.getLogger(UserService::class.java)

@Service
class EmailService(private val mailSender: JavaMailSender) {
    fun sendAuthEmail(email: String, token: String) {
        // TODO - Handle email send failure.
        val message = SimpleMailMessage().apply {
            setTo(email)
            from = EMAIL_FROM
            subject = EMAIL_SUBJECT
            text = EMAIL_MESSAGE + token
        }
        mailSender.send(message)
        analytics.info("SENDING $email with token: $token")
    }
}