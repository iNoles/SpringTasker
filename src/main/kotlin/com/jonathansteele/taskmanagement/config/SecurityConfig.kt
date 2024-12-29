package com.jonathansteele.taskmanagement.config

import com.jonathansteele.taskmanagement.JwtAuthenticationFilter
import com.jonathansteele.taskmanagement.JwtUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val env: Environment,
    private val jwtUtils: JwtUtils,
    private val userDetailsService: UserDetailsService
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationManager(authenticationConfiguration: AuthenticationConfiguration): AuthenticationManager {
        return authenticationConfiguration.authenticationManager
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val isDev = env.activeProfiles.contains("dev") // Check if 'dev' profile is active

        if (isDev) {
            // Disable frame options for the H2 console (required for iframe-based access)
            http.headers { it.frameOptions { frame -> frame.disable() } }
            // Allow access to H2 console in dev profile
            http.authorizeHttpRequests {
                it.requestMatchers("/h2-console/**").permitAll()
            }
        }

        http.csrf {
            it.ignoringRequestMatchers("/api/**") // Disable CSRF for API
            // CSRF enabled by default for form-based `/login` and `/register`
        }

        http.sessionManagement {
            it.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
        }

        http.authorizeHttpRequests {
            it.requestMatchers("/login", "/register").permitAll()
            it.requestMatchers("/api/auth/**").permitAll() // Allow API authentication endpoints
            it.anyRequest().authenticated() // Require authentication for all other endpoints
        }

        http.formLogin {
            it.loginPage("/login").permitAll() // Custom login page
            it.defaultSuccessUrl("/", true) // Redirect after successful login
        }

        http.logout {
            it.logoutUrl("/api/auth/logout") // POST endpoint for logout
            it.logoutSuccessUrl("/login?logout") // Redirect after logout
            it.invalidateHttpSession(true) // End session
            it.deleteCookies("JSESSIONID") // Clear session cookies
            it.permitAll()
        }

        http.addFilterBefore(
            JwtAuthenticationFilter(jwtUtils, userDetailsService),
            UsernamePasswordAuthenticationFilter::class.java
        )

        return http.build()
    }
}
