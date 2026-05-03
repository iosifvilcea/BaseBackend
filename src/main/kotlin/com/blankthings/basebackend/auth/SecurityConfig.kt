package com.blankthings.basebackend.auth

import com.blankthings.basebackend.user.AUTH_URL_PATH
import com.blankthings.basebackend.user.UserService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
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
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling {
                it.authenticationEntryPoint { _, response, exception ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, exception.message)
                }
            }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(HttpMethod.POST, "/api/auth").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/auth").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                    .anyRequest().authenticated()
            }
            .logout { logout ->
                logout
                    .logoutUrl("$AUTH_URL_PATH/logout")
                    .addLogoutHandler { request, response, _ ->
                        request.cookies
                            ?.find { it.name == REFRESH_TOKEN }
                            ?.value
                            ?.let { userService.logout(it) }

                        response.addHeader(HttpHeaders.SET_COOKIE, cookieManager.clearCookie(ACCESS_TOKEN, "/").toString())
                        response.addHeader(HttpHeaders.SET_COOKIE, cookieManager.clearCookie(REFRESH_TOKEN, AUTH_URL_PATH).toString())
                    }
                    .logoutSuccessHandler { _, response, _ -> response.status = 200 }
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
