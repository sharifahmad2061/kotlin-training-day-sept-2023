package org.fundamentals.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.fundamentals.model.Priority
import org.fundamentals.model.Task
import org.fundamentals.model.TaskRepository
import org.fundamentals.view.tasksAsTable
import java.lang.IllegalStateException

fun Application.configureRouting() {
    routing {
        staticResources("/tasks-ui", "tasks-ui")

        route("/tasks") {
            get {
                val tasks = TaskRepository.allTasks()
                call.respondText(
                    contentType = ContentType.parse("text/html"),
                    text = tasks.tasksAsTable()
                )
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
                call.respondText(
                    contentType = ContentType.parse("text/html"),
                    text = listOf(task).tasksAsTable()
                )
            }
            get("byPriority/{priority}"){
                val priority = call.parameters["priority"]
                if (priority == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }
                try {
                    val tasks = TaskRepository.tasksByPriority(Priority.valueOf(priority))
                    if (tasks.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }
                    call.respondText(
                        contentType = ContentType.parse("text/html"),
                        text = tasks.tasksAsTable()
                    )
                }
                catch (ex: IllegalArgumentException){
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
            post {
                val params = call.receiveParameters()
                val values = params.names().map { params[it] }
                if (values.any { it.isNullOrEmpty() } ) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }
                try {
                    val priority: Priority = values[2]?.let { it1 -> Priority.valueOf(it1) }!!
                    TaskRepository.addTask(
                        Task(values[0]!!, values[1]!!, priority)
                    )
                    call.respondRedirect("/tasks")
                }
                catch (ex: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }
                catch (ex: IllegalStateException) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }
            }
        }
    }
}
