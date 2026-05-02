package com.blankthings.basebackend

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.ErrorResponseException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(ex: AuthenticationException): ResponseEntity<String> =
        ResponseEntity(ex.message ?: "Unauthorized", HttpStatus.UNAUTHORIZED)

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(ex: AccessDeniedException): ResponseEntity<String> =
        ResponseEntity(ex.message ?: "Forbidden", HttpStatus.FORBIDDEN)

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(exception: UserNotFoundException): ResponseEntity<ErrorResponseException> =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponseException(HttpStatus.NOT_FOUND, exception))

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(ex: Exception): ResponseEntity<String> =
        ResponseEntity(ex.message ?: "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR)
}

class UserNotFoundException(email: String) : RuntimeException("User not found: $email")