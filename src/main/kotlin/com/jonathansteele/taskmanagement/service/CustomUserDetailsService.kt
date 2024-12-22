package com.jonathansteele.taskmanagement.service

import com.jonathansteele.taskmanagement.repository.UserRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository,
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val user =
            userRepository.findByUsername(username)
                ?: throw UsernameNotFoundException("User '$username' not found")
        return User(
            user.username,
            user.password,
            emptyList(), // Add authorities/roles here if needed
        )
    }
}
