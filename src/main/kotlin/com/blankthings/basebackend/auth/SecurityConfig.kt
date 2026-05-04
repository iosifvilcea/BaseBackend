package com.blankthings.basebackend.auth

import com.blankthings.basebackend.user.AUTH_URL_PATH
import com.blankthings.basebackend.user.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.Authentication
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val userService: UserService,
    private val cookieManager: CookieManager
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling {
                it.authenticationEntryPoint { _, response, exception ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, exception.message)
                }
            }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(HttpMethod.POST, AUTH_URL_PATH).permitAll()
                    .requestMatchers(HttpMethod.GET, AUTH_URL_PATH).permitAll()
                    .requestMatchers(HttpMethod.POST, "$AUTH_URL_PATH/refresh").permitAll()
                    .anyRequest().authenticated()
            }
            .logout { logout ->
                logout
                    .logoutUrl("$AUTH_URL_PATH/logout")
                    .addLogoutHandler(::clearAuthCookies)
                    .logoutSuccessHandler { _, response, _ -> response.status = HttpStatus.OK.value() }
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()

    private fun clearAuthCookies(request: HttpServletRequest, response: HttpServletResponse, auth: Authentication?) {
        request.cookies
            ?.find { it.name == REFRESH_TOKEN }
            ?.value
            ?.let(userService::logout)

        response.addHeader(HttpHeaders.SET_COOKIE, cookieManager.clearCookie(ACCESS_TOKEN, "/").toString())
        response.addHeader(HttpHeaders.SET_COOKIE, cookieManager.clearCookie(REFRESH_TOKEN, AUTH_URL_PATH).toString())
    }
}
