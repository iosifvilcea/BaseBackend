package com.blankthings.basebackend

import com.blankthings.basebackend.profile.Profile
import com.blankthings.basebackend.profile.ProfileRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val logger = LoggerFactory.getLogger(BaseBackendApplication::class.java)

@RestController
@SpringBootApplication
class BaseBackendApplication {
    @RequestMapping("/")
    fun home() = "Hello World."

//    @Bean
//    fun demo(repository: ProfileRepository) = CommandLineRunner {
//        repository.saveAll(listOf(
//            Profile(id = 1L, username = "RockMan", email = "rockman@mail.com"),
//            Profile(username = "boodyball", email = "boodyball@mail.com"),
//            Profile(username = "markus", email = "markus@mail.com"),
//            Profile(username = "jocker255", email = "joker255@mail.com"),
//        ))
//
//        logger.info("Profiles:")
//        repository.findAll().forEach { logger.info("Profile: $it") }
//
//        val profile = repository.findById(1L).orElseThrow()
//        logger.info("ID 1L: $profile")
//    }
}

fun main(args: Array<String>) {
    runApplication<BaseBackendApplication>(*args)
}
