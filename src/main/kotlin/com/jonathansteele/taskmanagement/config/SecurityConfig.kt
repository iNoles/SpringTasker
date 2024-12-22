package com.jonathansteele.taskmanagement.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val env: Environment,
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        val isDev = env.activeProfiles.contains("dev") // Check if 'dev' profile is active

        if (isDev) {
            // Disable frame options for the H2 console (required for iframe-based access)
            http.headers { it.frameOptions { frame -> frame.disable() } }
            // Disable CSRF for the H2 console
            http.csrf { csrf -> csrf.disable() }
            // Allow access to H2 console in dev profile
            http.authorizeHttpRequests { auth ->
                auth.requestMatchers("/h2-console/**").permitAll()
            }
        }

        http
            .authorizeHttpRequests { auth ->
                auth.requestMatchers("/auth/register", "/auth/login").permitAll() // Allow registration and login
                auth.anyRequest().authenticated() // Require authentication for all other endpoints
            }.formLogin { form ->
                form
                    .loginPage("/auth/login") // Custom login page
                    .permitAll() // Allow all users to access login page
                    .defaultSuccessUrl("/", true) // Redirect to home page after login
                    .failureUrl("/auth/login?error=true") // Redirect back to login page on error
            }.logout { logout ->
                logout
                    .logoutUrl("/auth/logout")
                    .logoutSuccessUrl("/auth/login?logout")
                    .permitAll() // Allow all users to log out
            }

        return http.build()
    }
}
