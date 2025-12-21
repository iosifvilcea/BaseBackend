package com.blankthings.basebackend.analytics

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AnalyticsTracker {
    private val logger: Logger = LoggerFactory.getLogger(AnalyticsTracker::class.java)
    fun track(event: AnalyticsEvent, properties: Any) {
        logger.info("$event : $properties")
    }
}