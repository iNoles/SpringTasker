package com.jonathansteele.taskmanagement.controller

import com.jonathansteele.taskmanagement.JwtUtils
import com.jonathansteele.taskmanagement.model.User
import com.jonathansteele.taskmanagement.repository.UserRepository
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
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
            // Authenticate the user with the provided credentials
            val authentication =
                authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken(username, password),
                )
            SecurityContextHolder.getContext().authentication = authentication

            // Generate JWT token for the authenticated user
            val cookie = jwtUtils.generateJwtCookie(authentication)

            // Add the cookie to the response header
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())

            ResponseEntity.ok(mapOf("message" to "Login successful"))
        } catch (e: BadCredentialsException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(mapOf("error" to "Invalid username or password"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(mapOf("error" to "An unexpected error occurred"))
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
        if (password.length < 8) { // Example password policy
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
        // Clear JWT token
        val cookie = jwtUtils.clearJwtCookie()
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())

        // Invalidate session (if any)
        SecurityContextHolder.clearContext()

        return ResponseEntity.ok(mapOf("message" to "Logged out successfully"))
    }
}
