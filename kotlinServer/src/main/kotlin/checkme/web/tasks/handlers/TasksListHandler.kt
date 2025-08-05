package checkme.web.tasks.handlers

import checkme.domain.models.User
import checkme.domain.operations.tasks.TaskOperationsHolder
import checkme.web.solution.handlers.ViewCheckResultError
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.core.*
import org.http4k.lens.RequestContextLens

class TasksListHandler(
    private val taskOperations: TaskOperationsHolder,
    private val userLens: RequestContextLens<User?>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)
        return when {
            user == null -> Response(Status.BAD_REQUEST)
                .body(objectMapper.writeValueAsString(mapOf("error" to ViewCheckResultError.USER_HAS_NOT_RIGHTS)))

            else -> {
    }
}