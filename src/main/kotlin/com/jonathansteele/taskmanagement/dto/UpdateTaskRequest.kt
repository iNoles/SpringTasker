package com.jonathansteele.taskmanagement.dto

data class UpdateTaskRequest(
    val name: String? = null,
    val description: String? = null,
    val dueDate: String? = null,
    val priority: String? = null,
    val userId: Long? = null // Add userId field
)