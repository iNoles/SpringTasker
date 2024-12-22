package com.jonathansteele.taskmanagement.dto

data class TaskResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val dueDate: String, // ISO format
    val priority: String
)
