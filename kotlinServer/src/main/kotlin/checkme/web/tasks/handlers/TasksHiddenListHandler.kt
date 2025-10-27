package checkme.web.tasks.handlers

import checkme.domain.models.User
import checkme.domain.operations.tasks.TaskFetchingError
import checkme.domain.operations.tasks.TaskOperationsHolder
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.solution.handlers.ViewCheckResultError
import checkme.web.tasks.forms.TasksListData
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
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

private fun fetchHiddenTasksData(taskOperations: TaskOperationsHolder): Result<List<TasksListData>, FetchingTaskError> {
    return when (
        val fetchedTasks = taskOperations.fetchHiddenTasksIdAndName()
    ) {
        is Success -> Success(fetchedTasks.value)
        is Failure -> when (fetchedTasks.reason) {
            TaskFetchingError.NO_SUCH_TASK -> Failure(FetchingTaskError.NO_SUCH_TASK)
            TaskFetchingError.UNKNOWN_DATABASE_ERROR -> Failure(FetchingTaskError.UNKNOWN_DATABASE_ERROR)
        }
    }
}

private fun tryFetchTasks(
    taskOperations: TaskOperationsHolder,
    objectMapper: ObjectMapper,
): Response {
    return when (
        val tasks = fetchHiddenTasksData(taskOperations)
    ) {
        is Failure -> objectMapper.sendBadRequestError(tasks.reason)
        is Success -> objectMapper.sendOKResponse(tasks.value)
    }
}
