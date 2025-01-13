package com.jonathansteele.taskmanagement

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.SecurityException
import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import org.springframework.web.util.WebUtils
import java.security.Key
import java.util.Date
import javax.crypto.spec.SecretKeySpec

@Component
class JwtUtils {
    @Value("\${jwt.secret}")
    private lateinit var jwtSecretString: String
    private lateinit var signingKey: Key

    @PostConstruct
    fun init() {
        signingKey = SecretKeySpec(jwtSecretString.toByteArray(), SignatureAlgorithm.HS512.jcaName)
    }

    val jwtExpirationMs = 900_000L // 15 minutes
    private val refreshExpirationMs = 7L * 24 * 60 * 60 * 1000 // 7 days
    private val jwtCookie = "JWT"
    private val refreshCookieName = "refresh_token"

    private fun getSigningKey(): Key = signingKey

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

    fun generateJwtToken(username: String): String =
        Jwts
            .builder()
            .setSubject(username)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + jwtExpirationMs))
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact()

    fun generateRefreshToken(username: String): String =
        Jwts
            .builder()
            .setSubject(username)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + refreshExpirationMs))
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact()

    fun getUsernameFromToken(token: String): String =
        Jwts
            .parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .body.subject

    fun validateToken(token: String): Boolean =
        try {
            Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
            true
        } catch (e: ExpiredJwtException) {
            false
        } catch (e: SecurityException) {
            false
        } catch (e: Exception) {
            false
        }
}
