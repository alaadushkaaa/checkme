package checkme.web.tasks.handlers

import checkme.domain.models.User
import checkme.domain.operations.tasks.TaskOperationsHolder
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.solution.handlers.ViewCheckResultError
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.*
import org.http4k.lens.RequestContextLens

class TasksHiddenListHandler(
    private val taskOperations: TaskOperationsHolder,
    private val userLens: RequestContextLens<User?>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)
        return when {
            user == null || !user.isAdmin() ->
                objectMapper
                    .sendBadRequestError(ViewCheckResultError.USER_HAS_NOT_RIGHTS)

            else -> {
                tryFetchTasks(
                    taskOperations = taskOperations,
                    objectMapper = objectMapper
                )
            }
        }
    }
}

private fun tryFetchTasks(
    taskOperations: TaskOperationsHolder,
    objectMapper: ObjectMapper,
): Response {
    return when (
        val tasks = fetchHiddenTasks(taskOperations)
    ) {
        is Failure -> objectMapper.sendBadRequestError(tasks.reason)
        is Success -> objectMapper.sendOKResponse(tasks.value)
    }
}
