package com.jonathansteele.taskmanagement.model

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "tasks")
data class Task(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String,

    @Column(length = 500)
    val description: String? = null,

    @Column(nullable = false)
    val dueDate: LocalDate,

    @Column(nullable = false)
    val priority: String, // Could be "Low", "Medium", "High"

    @Column(nullable = false)
    val completed: Boolean = false, // Add the completed field with a default value

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User
)
