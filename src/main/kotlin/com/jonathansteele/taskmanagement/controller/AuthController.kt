package com.jonathansteele.taskmanagement.controller

import com.jonathansteele.taskmanagement.JwtUtils
import com.jonathansteele.taskmanagement.model.User
import com.jonathansteele.taskmanagement.repository.UserRepository
import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtils: JwtUtils,
) {
    @PostMapping("/login")
    fun loginUser(
        @RequestParam username: String,
        @RequestParam password: String,
        response: HttpServletResponse,
    ): ResponseEntity<Map<String, String>> =
        try {
            val authentication =
                authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken(username, password),
                )
            SecurityContextHolder.getContext().authentication = authentication

            // Use correct JWT token generation method
            val jwtCookie = jwtUtils.generateJwtCookie(username)
            val refreshCookie = jwtUtils.generateRefreshCookie(username)

            response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString())
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString())

            ResponseEntity.ok(mapOf("message" to "Login successful"))
        } catch (e: BadCredentialsException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "Invalid username or password"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "An unexpected error occurred"))
        }

    @PostMapping("/refresh")
    fun refreshAccessToken(
        @CookieValue(name = "refresh_token", required = false) refreshToken: String?,
        response: HttpServletResponse
    ): ResponseEntity<Map<String, String>> {
        if (refreshToken.isNullOrBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "Refresh token is missing"))
        }

        try {
            if (!jwtUtils.validateToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "Invalid refresh token"))
            }

            val username = jwtUtils.getUsernameFromToken(refreshToken)
            val newAccessToken = jwtUtils.generateJwtToken(username)
            val jwtCookie = jwtUtils.generateJwtCookie(username)

            response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString())

            return ResponseEntity.ok(
                mapOf(
                    "access_token" to newAccessToken,
                    "expires_in" to (jwtUtils.jwtExpirationMs / 1000).toString()
                )
            )
        } catch (e: ExpiredJwtException) {
            val expiredRefreshCookie = jwtUtils.clearRefreshCookie()
            response.addHeader(HttpHeaders.SET_COOKIE, expiredRefreshCookie.toString())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "Refresh token expired"))
        } catch (e: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "Unexpected error occurred"))
        }
    }

    @PostMapping("/register")
    fun registerUser(
        @RequestParam username: String,
        @RequestParam email: String,
        @RequestParam password: String,
    ): ResponseEntity<Map<String, String>> {
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to "Username already exists"))
        }
        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to "Email already exists"))
        }
        if (!email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Invalid email format"))
        }
        if (password.length < 8) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Password must be at least 8 characters"))
        }

        val encodedUser =
            User(
                username = username,
                email = email,
                password = passwordEncoder.encode(password),
            )
        userRepository.save(encodedUser)
        return ResponseEntity.status(HttpStatus.CREATED).body(mapOf("message" to "Registration successful"))
    }

    @PostMapping("/logout")
    fun logoutUser(response: HttpServletResponse): ResponseEntity<Map<String, String>> {
        val jwtCookie = jwtUtils.clearJwtCookie()
        val refreshCookie = jwtUtils.clearRefreshCookie()

        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString())
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString())

        SecurityContextHolder.clearContext()

        return ResponseEntity.ok(mapOf("message" to "Logged out successfully"))
    }
}
