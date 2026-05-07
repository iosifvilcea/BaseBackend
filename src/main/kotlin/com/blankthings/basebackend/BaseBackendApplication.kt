package com.blankthings.basebackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@SpringBootApplication
class BaseBackendApplication {
    @RequestMapping("/")
    fun home() = "Hello World."
}

fun main(args: Array<String>) {
    runApplication<BaseBackendApplication>(*args)
}
