package com.jonathansteele.taskmanagement.controller

import com.jonathansteele.taskmanagement.model.Task
import com.jonathansteele.taskmanagement.model.User
import com.jonathansteele.taskmanagement.repository.TaskRepository
import com.jonathansteele.taskmanagement.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import org.springframework.security.core.userdetails.User as SpringUser

@RestController
@RequestMapping("/api/tasks")
class TaskController(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
) {
    // Get all tasks for the current user
    @GetMapping
    fun getAllTasks(): ResponseEntity<out Any>? {
        val currentUser =
            getCurrentUser() ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user not found")
        val tasks = taskRepository.findByUser(currentUser)
        return ResponseEntity.ok(tasks)
    }

    // Handle the form submission to add the task
    @PostMapping("/add")
    fun addTask(
        @RequestParam(name = "name") name: String,
        @RequestParam(name = "description", required = false) description: String?,
        @RequestParam(name = "due-date") dueDate: String,
        @RequestParam(name = "priority") priority: String,
        @RequestParam(name = "completed") completed: Boolean,
    ): ResponseEntity<String> {
        // Get the currently logged-in user
        val currentUser =
            getCurrentUser() ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body("user not found")

        // Parse the due date
        val parsedDueDate = LocalDate.parse(dueDate)

        // Create a new task
        val newTask =
            Task(
                name = name,
                description = description,
                dueDate = parsedDueDate,
                priority = priority,
                completed = completed,
                user = currentUser,
            )

        // Save the task to the repository
        taskRepository.save(newTask)

        return ResponseEntity.status(HttpStatus.CREATED).body("Task added successfully!")
    }

    @PutMapping("/{id}")
    fun updateTask(
        @RequestParam id: Long,
        @RequestParam name: String,
        @RequestParam description: String?,
        @RequestParam(name = "due-date") dueDate: String,
        @RequestParam priority: String,
        @RequestParam(required = false) completed: Boolean,
    ): ResponseEntity<String> {
        if (!taskRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task with ID $id not found")
        }
        val task = taskRepository.findById(id).get()
        val updatedTask =
            task.copy(
                name = name,
                description = description,
                dueDate = LocalDate.parse(dueDate),
                priority = priority,
                completed = completed,
            )
        taskRepository.save(updatedTask)
        return ResponseEntity.ok("Task updated successfully!")
    }

    @DeleteMapping("/{id}")
    fun deleteTask(
        @PathVariable id: Long,
    ): ResponseEntity<String> {
        if (!taskRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task with ID $id not found")
        }
        taskRepository.deleteById(id)
        return ResponseEntity.ok("Task deleted successfully!")
    }

    // Get the currently logged-in user
    private fun getCurrentUser(): User? {
        val username = (SecurityContextHolder.getContext().authentication?.principal as? SpringUser)?.username
        return userRepository.findByUsername(username)
    }
}
