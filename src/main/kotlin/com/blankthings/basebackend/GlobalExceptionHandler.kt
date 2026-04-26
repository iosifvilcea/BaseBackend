package com.blankthings.basebackend

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.ErrorResponseException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(Exception::class)
    fun handleGeneralException(ex: Exception?): ResponseEntity<String> {
        val error = ex?.message ?: "Internal server error"
        return ResponseEntity<String>(error, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(exception: UserNotFoundException): ResponseEntity<ErrorResponseException> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponseException(HttpStatus.NOT_FOUND, exception))
    }
}