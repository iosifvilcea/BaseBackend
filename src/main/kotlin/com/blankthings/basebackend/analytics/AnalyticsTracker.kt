package com.blankthings.basebackend.analytics

import org.slf4j.Logger
import org.slf4j.LoggerFactory

// TODO - Track events of all calls.
object AnalyticsTracker {
    private val logger: Logger = LoggerFactory.getLogger(AnalyticsTracker::class.java)
    fun track(event: AnalyticsEvent, vararg properties: Any) {
        logger.info("$event : $properties")
    }
}