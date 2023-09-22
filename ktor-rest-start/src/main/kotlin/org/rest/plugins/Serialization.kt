package org.rest.plugins

import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.rest.model.Priority
import org.rest.model.Task
import org.rest.model.TaskRepository

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
    routing {
        route("/tasks") {
            get {
                val tasks = TaskRepository.allTasks()
                call.respond(tasks)
            }
            get("/byName/{taskName}") {
                val name = call.parameters["taskName"]
                if (name == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
                val task = TaskRepository.taskByName(name)
                if (task == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }
                call.respond(task)
            }
            get("/byPriority/{priority}") {
                val priorityAsText = call.parameters["priority"]
                if (priorityAsText == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
                try {
                    val priority = Priority.valueOf(priorityAsText)
                    val tasks = TaskRepository.tasksByPriority(priority)

                    if (tasks.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    call.respond(tasks)
                } catch (ex: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
            post {
                val task = call.receive<Task>()
                TaskRepository.addTask(task)
                call.respond(HttpStatusCode.OK)
                return@post
            }
            delete("/{taskName}") {
                val taskName = call.parameters["taskName"]
                if (taskName.isNullOrEmpty()){
                    call.respond(HttpStatusCode.BadRequest)
                    return@delete
                }
                else {
                    TaskRepository.removeTask(taskName!!)
                    call.respond(HttpStatusCode.OK)
                    return@delete
                }
            }
        }
    }
}
