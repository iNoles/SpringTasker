package com.jonathansteele.taskmanagement

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.SecurityException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseCookie
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.web.util.WebUtils
import java.security.Key
import java.util.Date
import javax.crypto.spec.SecretKeySpec

@Component
class JwtUtils {
    @Value("\${jwt.secret}")
    private lateinit var jwtSecret: String

    val jwtExpirationMs = 3600000 // 1 hour in milliseconds

    val jwtCookie = "JWT"

    private fun getSigningKey(): Key = SecretKeySpec(jwtSecret.toByteArray(), SignatureAlgorithm.HS512.jcaName)

    fun getJwtFromCookies(request: HttpServletRequest): String? =
        WebUtils.getCookie(request, jwtCookie)?.let {
            it.value ?: null
        }

    // Generate JWT cookie for authenticated user
    fun generateJwtCookie(authentication: Authentication): ResponseCookie {
        val userDetails = authentication.principal as UserDetails
        val jwt = generateJwtTokenFromUsername(userDetails.username)
        return ResponseCookie
            .from(jwtCookie, jwt)
            .path("/")
            .maxAge(24 * 60 * 60)
            .httpOnly(true)
            .build()
    }

    // Clear JWT cookie (on logout)
    fun clearJwtCookie(): ResponseCookie =
        ResponseCookie
            .from(jwtCookie, "") // Set value to an empty string
            .path("/") // Cookie path for clear action
            .maxAge(0) // Expiry set to 0 to delete the cookie immediately
            .build()

    // Generate JWT token from username
    private fun generateJwtTokenFromUsername(username: String): String =
        Jwts
            .builder()
            .setSubject(username)
            .setIssuedAt(Date())
            .setExpiration(Date(Date().time + jwtExpirationMs))
            .signWith(getSigningKey(), SignatureAlgorithm.HS512) // Updated method
            .compact()

    // Get username from JWT token
    fun getUsernameFromJwtToken(token: String): String {
        val claims =
            Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .body
        return claims.subject
    }

    // Validate JWT token
    fun validateJwtToken(token: String): Boolean =
        try {
            Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
            true
        } catch (e: ExpiredJwtException) {
            println("Token expired: ${e.message}")
            false
        } catch (e: SecurityException) {
            println("JWT security error: ${e.message}")
            false
        } catch (e: Exception) {
            println("JWT validation error: ${e.message}")
            false
        }
}
