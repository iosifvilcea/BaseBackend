package com.blankthings.basebackend.analytics

import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class AnalyticsTracker(
    private val meterRegistry: MeterRegistry,
) {
    private val logger = LoggerFactory.getLogger(AnalyticsTracker::class.java)

    fun track(
        event: AnalyticsEvent,
        vararg properties: Any,
    ) {
        logger
            .atInfo()
            .addKeyValue("event", event.name)
            .addKeyValue("properties", properties.joinToString())
            .log("Analytics event")
        meterRegistry.counter("analytics.events", "event", event.name).increment()
    }
}
