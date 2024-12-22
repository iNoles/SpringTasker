package com.jonathansteele.taskmanagement.dto

data class CreateTaskRequest(
    val name: String,
    val description: String,
    val dueDate: String, // ISO format (e.g., "2024-12-31")
    val priority: String,
    val userId: Long // Add userId field
)