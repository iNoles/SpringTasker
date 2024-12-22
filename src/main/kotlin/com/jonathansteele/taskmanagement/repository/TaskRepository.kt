package com.jonathansteele.taskmanagement.repository

import com.jonathansteele.taskmanagement.model.Task
import com.jonathansteele.taskmanagement.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TaskRepository : JpaRepository<Task, Long> {
    fun findByUser(user: User): List<Task> // Find tasks by user
}
