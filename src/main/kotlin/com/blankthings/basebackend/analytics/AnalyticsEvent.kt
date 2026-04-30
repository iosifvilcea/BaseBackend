package com.blankthings.basebackend.analytics

// TODO - Create Events of all calls.
enum class AnalyticsEvent {
    DEBUG,

    USER_CREATED,
    USER_DELETED,
    USER_UPDATED,
    USER_FETCHED,
    USERS_FETCHED,

    AUTH_SUCCESSFUL,
    AUTH_FAILED,

    EMAIL_SENT,

    LOGOUT_SUCCESSFUL,
    LOGOUT_FAILED,
}