package com.jonathansteele.taskmanagement.dto

data class CreateTaskRequest(
    val name: String,
    val description: String,
    // ISO format (e.g., "2024-12-31")
    val dueDate: String,
    val priority: String,
    val userId: Long,
)
