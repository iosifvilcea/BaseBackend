package com.blankthings.basebackend.auth

import com.blankthings.basebackend.analytics.AnalyticsEvent
import com.blankthings.basebackend.analytics.AnalyticsTracker
import com.blankthings.basebackend.user.UserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

private const val PATH_API_AUTH = "/api/auth"
private const val PATH_API_REFRESH = "/api/auth/refresh"
private const val PATH_API_LOGOUT = "/api/auth/logout"


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
        try {
            authenticate(request)
        } catch (e: Exception) {
            AnalyticsTracker.track(AnalyticsEvent.AUTH_ERROR, "Error authenticating cookies.", e)
            SecurityContextHolder.clearContext()
        } finally {
            chain.doFilter(request, response)
        }
    }

    private fun authenticate(request: HttpServletRequest) {
        request.cookies
            ?.find { it.name == ACCESS_TOKEN }?.value
            ?.let { jwtService.validateToken(it) }
            ?.let { userId -> userRepository.findById(userId).orElse(null) }
            ?.let { user ->
                UsernamePasswordAuthenticationToken(user, null, emptyList()).apply {
                    details = WebAuthenticationDetailsSource().buildDetails(request)
                }
            }?.also { auth -> SecurityContextHolder.getContext().authentication = auth }
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        // Skip JWT check for public endpoints to avoid unnecessary DB hits
        return when (request.servletPath) {
            PATH_API_AUTH,
            PATH_API_REFRESH,
            PATH_API_LOGOUT -> true
            else -> false
        }
    }
}