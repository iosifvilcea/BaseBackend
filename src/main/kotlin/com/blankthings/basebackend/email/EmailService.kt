package com.blankthings.basebackend.email

import com.blankthings.basebackend.analytics.AnalyticsEvent
import com.blankthings.basebackend.analytics.AnalyticsTracker
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.MailException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

const val EMAIL_FROM = "no-reply@blankthings.com"
const val EMAIL_SUBJECT = "Login link for blankthings.com"
const val EMAIL_MESSAGE = "Here's your login for blankthings.com:\n\n %s/api/auth?token="

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    private val analyticsTracker: AnalyticsTracker,
) {
    private val logger = LoggerFactory.getLogger(EmailService::class.java)

    @Value("\${app.url}")
    lateinit var url: String

    fun sendAuthEmail(
        email: String,
        token: String,
    ) {
        val normalizedEmail = email.trim().lowercase()
        val message =
            SimpleMailMessage().apply {
                setTo(normalizedEmail)
                from = EMAIL_FROM
                subject = EMAIL_SUBJECT
                text = EMAIL_MESSAGE.format(url) + token
            }

        try {
            mailSender.send(message)
            analyticsTracker.track(AnalyticsEvent.EMAIL_SENT, normalizedEmail)
        } catch (ex: MailException) {
            analyticsTracker.track(AnalyticsEvent.EMAIL_FAILED, normalizedEmail)
            logger.error("Failed to send auth email to {}", normalizedEmail, ex)
            throw EmailDeliveryException()
        }
    }
}
