package com.jonathansteele.taskmanagement.controller

import com.jonathansteele.taskmanagement.model.Task
import com.jonathansteele.taskmanagement.model.User
import com.jonathansteele.taskmanagement.repository.TaskRepository
import com.jonathansteele.taskmanagement.repository.UserRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User as SpringUser
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDate

@Controller
class TaskController(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository
) {
    // Show the form for adding a task
    @GetMapping("/tasks/add")
    fun showAddTaskForm(): String {
        return "addTask"
    }

    // Handle the form submission to add the task
    @PostMapping("/tasks/add")
    fun addTask(
        @RequestParam(name = "name") name: String,
        @RequestParam(name = "description", required = false) description: String?,
        @RequestParam(name = "dueDate") dueDate: String,
        @RequestParam(name = "priority") priority: String,
        @RequestParam(name = "completed") completed: Boolean,
        model: Model
    ): String {
        // Get the currently logged-in user
        val currentUser = getCurrentUser()

        // Parse the due date
        val parsedDueDate = LocalDate.parse(dueDate)

        // Create a new task
        val newTask = Task(
            name = name,
            description = description,
            dueDate = parsedDueDate,
            priority = priority,
            completed = completed,
            user = currentUser
        )

        // Save the task to the repository
        taskRepository.save(newTask)

        // Add success message to model
        model.addAttribute("success", "Task added successfully!")

        // Redirect to the task list page or some other page
        return "redirect:/"
    }

    @GetMapping("/tasks/edit/{id}")
    fun editTaskForm(@PathVariable id: Long, model: Model): String {
        val task = taskRepository.findById(id).orElseThrow { IllegalArgumentException("Invalid task ID: $id") }
        model.addAttribute("task", task)
        return "editTask"
    }

    @PostMapping("/tasks/edit")
    fun updateTask(
        @RequestParam id: Long,
        @RequestParam name: String,
        @RequestParam description: String?,
        @RequestParam dueDate: String,
        @RequestParam priority: String,
        @RequestParam(required = false) completed: Boolean
    ): String {
        val task = taskRepository.findById(id).orElseThrow { IllegalArgumentException("Invalid task ID: $id") }
        taskRepository.save(task.copy(
            name = name,
            description = description,
            dueDate = LocalDate.parse(dueDate),
            priority = priority,
            completed = completed
        ))
        return "redirect:/"
    }

    @PostMapping("/tasks/delete/{id}")
    fun deleteTask(@PathVariable id: Long): String {
        taskRepository.deleteById(id)
        return "redirect:/"
    }

    // Get the currently logged-in user
    private fun getCurrentUser(): User {
        val username = (SecurityContextHolder.getContext().authentication.principal as SpringUser).username
        return userRepository.findByUsername(username)!!
    }
}
