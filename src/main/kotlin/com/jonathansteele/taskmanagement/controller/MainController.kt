package com.jonathansteele.taskmanagement.controller

import com.jonathansteele.taskmanagement.model.User
import com.jonathansteele.taskmanagement.repository.TaskRepository
import com.jonathansteele.taskmanagement.repository.UserRepository
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class MainController(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository
) {

    @GetMapping("/")
    fun mainPage(model: Model): String {
        val currentUser = getCurrentUser()  // Get the current user
        val tasks = taskRepository.findByUser(currentUser)  // Find tasks associated with the current user
        model.addAttribute("tasks", tasks)  // Add tasks to the model

        return "index"  // Return the Thymeleaf template name
    }

    private fun getCurrentUser(): User {
        val authentication: Authentication = SecurityContextHolder.getContext().authentication
        val username = authentication.name  // Get the username of the authenticated user
        return userRepository.findByUsername(username)  // Get the User object from the database
            ?: throw IllegalStateException("User not found")  // Handle case where user is not found
    }
}

