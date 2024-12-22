package com.jonathansteele.taskmanagement

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TaskmanagementApplication

fun main(args: Array<String>) {
	runApplication<TaskmanagementApplication>(*args)
}
