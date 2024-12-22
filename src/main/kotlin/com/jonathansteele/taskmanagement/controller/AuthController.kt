package com.jonathansteele.taskmanagement.controller

import com.jonathansteele.taskmanagement.model.User
import com.jonathansteele.taskmanagement.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
class AuthController(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    @GetMapping("/auth/login")
    fun loginPage(
        @RequestParam(required = false) error: Boolean,
        model: Model,
    ): String {
        if (error) {
            model.addAttribute("error", "Invalid username or password.")
        }
        return "login"
    }

    @PostMapping("/auth/login")
    fun loginUser(
        @RequestParam username: String,
        @RequestParam password: String,
        model: Model,
    ): String {
        val user = userRepository.findByUsername(username)
        return if (user != null && passwordEncoder.matches(password, user.password)) {
            model.addAttribute("success", "Login successful!")
            "redirect:/"
        } else {
            model.addAttribute("error", "Invalid username or password.")
            "login"
        }
    }

    @GetMapping("/auth/register")
    fun registerPage(): String {
        return "register" // Thymeleaf template for registration
    }

    @PostMapping("/auth/register")
    fun registerUser(
        @RequestParam username: String,
        @RequestParam email: String,
        @RequestParam password: String,
        redirectAttributes: RedirectAttributes,
    ): String {
        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            redirectAttributes.addFlashAttribute("error", "Username already exists")
            return "redirect:/auth/register"
        }

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("error", "Email already exists")
            return "redirect:/auth/register"
        }

        // Save the new user
        val encodedUser =
            User(
                username = username,
                email = email,
                password = passwordEncoder.encode(password),
            )
        userRepository.save(encodedUser)

        // Add success message for redirect
        redirectAttributes.addFlashAttribute("success", "Registration successful!")
        return "redirect:/auth/login"
    }
}
