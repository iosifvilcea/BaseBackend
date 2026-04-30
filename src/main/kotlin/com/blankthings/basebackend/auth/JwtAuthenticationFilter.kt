package com.blankthings.basebackend.auth

import com.blankthings.basebackend.user.UserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userRepository: UserRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        request.cookies
            ?.find { it.name == "access_token" }
            ?.value
            ?.let { jwtService.validateToken(it) }
            ?.let { userId -> userRepository.findById(userId).orElse(null) }
            ?.let { user -> UsernamePasswordAuthenticationToken(user, null, emptyList()) }
            ?.also { auth -> SecurityContextHolder.getContext().authentication = auth }

        chain.doFilter(request, response)
    }
}
