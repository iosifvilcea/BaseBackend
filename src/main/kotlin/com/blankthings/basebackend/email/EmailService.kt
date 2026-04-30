package com.blankthings.basebackend.email

import com.blankthings.basebackend.analytics.AnalyticsEvent
import com.blankthings.basebackend.analytics.AnalyticsTracker
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

const val EMAIL_FROM = "no-reply@blankthings.com"
const val EMAIL_SUBJECT = "Login link for blankthings.com"
const val EMAIL_MESSAGE = "Here's your login for blankthings.com:\n\n https://blankthings.com/api/auth?token="

@Service
class EmailService(private val mailSender: JavaMailSender) {
    fun sendAuthEmail(email: String, token: String) {
        val normalizedEmail = email.trim().lowercase()
        val message = SimpleMailMessage().apply {
            setTo(normalizedEmail)
            from = EMAIL_FROM
            subject = EMAIL_SUBJECT
            text = EMAIL_MESSAGE + token
        }

        mailSender.send(message)
        AnalyticsTracker.track(AnalyticsEvent.EMAIL_SENT, "SENDING $email with token: $token")
    }
}