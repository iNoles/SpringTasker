package com.jonathansteele.taskmanagement.dto

data class TaskResponse(
    val id: Long,
    val name: String,
    val description: String?,
    // ISO format
    val dueDate: String,
    val priority: String,
)
