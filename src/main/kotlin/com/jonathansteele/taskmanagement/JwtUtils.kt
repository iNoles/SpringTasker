package com.jonathansteele.taskmanagement

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SecurityException
import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import org.springframework.web.util.WebUtils
import java.util.Base64
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtUtils {
    @Value("\${jwt.secret}")
    private lateinit var jwtSecretString: String
    private lateinit var signingKey: SecretKey

    @PostConstruct
    fun init() {
        // Ensure the secret is properly converted into a 256-bit (32-byte) key for HS256
        val keyBytes = Base64.getDecoder().decode(jwtSecretString)
        signingKey = Keys.hmacShaKeyFor(keyBytes)
    }

    val jwtExpirationMs = 900_000L // 15 minutes
    private val refreshExpirationMs = 7L * 24 * 60 * 60 * 1000 // 7 days
    private val jwtCookie = "JWT"
    private val refreshCookieName = "refresh_token"

    fun getJwtFromCookies(request: HttpServletRequest): String? = WebUtils.getCookie(request, jwtCookie)?.value

    fun generateJwtCookie(username: String): ResponseCookie {
        val jwt = generateJwtToken(username)
        return ResponseCookie
            .from(jwtCookie, jwt)
            .path("/")
            .maxAge(jwtExpirationMs / 1000)
            .httpOnly(true)
            .build()
    }

    fun generateRefreshCookie(username: String): ResponseCookie {
        val refreshToken = generateRefreshToken(username)
        return ResponseCookie
            .from(refreshCookieName, refreshToken)
            .path("/")
            .httpOnly(true)
            .maxAge(refreshExpirationMs / 1000)
            .build()
    }

    fun clearJwtCookie(): ResponseCookie =
        ResponseCookie
            .from(jwtCookie, "")
            .path("/")
            .maxAge(0)
            .build()

    fun clearRefreshCookie(): ResponseCookie =
        ResponseCookie
            .from(refreshCookieName, "")
            .path("/")
            .httpOnly(true)
            .maxAge(0)
            .build()

    fun generateJwtToken(username: String): String {
        return Jwts
            .builder()
            .subject(username)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + jwtExpirationMs))
            .signWith(signingKey)
            .compact()
    }

    fun generateRefreshToken(username: String): String =
        Jwts
            .builder()
            .subject(username)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + refreshExpirationMs))
            .signWith(signingKey)
            .compact()

    fun getUsernameFromToken(token: String): String =
        Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject

    fun validateToken(token: String): Boolean =
        try {
            Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: ExpiredJwtException) {
            false
        } catch (e: SecurityException) {
            false
        } catch (e: Exception) {
            false
        }
}
