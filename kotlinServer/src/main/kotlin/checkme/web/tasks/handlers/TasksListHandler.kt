package checkme.web.tasks.handlers

import checkme.domain.models.User
import checkme.domain.operations.tasks.TaskOperationsHolder
import checkme.web.solution.forms.ResultResponse
import checkme.web.solution.forms.TaskResultResponse
import checkme.web.solution.handlers.ViewCheckResultError
import checkme.web.solution.handlers.sendBadRequestError
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.core.*
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.lens.RequestContextLens

class TasksListHandler(
    private val taskOperations: TaskOperationsHolder,
    private val userLens: RequestContextLens<User?>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)
        return when {
            user == null -> objectMapper.sendBadRequestError(ViewCheckResultError.USER_HAS_NOT_RIGHTS)

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
) : Response {
    return when (
        val tasks = fetchAllTasksIdAndName(taskOperations)
    ) {
        is Failure -> objectMapper.sendBadRequestError(tasks.reason)
        is Success -> Response(Status.OK).body(objectMapper.writeValueAsString(tasks.value))
    }
}