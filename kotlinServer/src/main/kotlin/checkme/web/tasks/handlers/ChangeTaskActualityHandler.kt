package checkme.web.tasks.handlers

import checkme.domain.models.Task
import checkme.domain.models.User
import checkme.domain.operations.tasks.ModifyTaskError
import checkme.domain.operations.tasks.TaskOperationsHolder
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.lenses.GeneralWebLenses.idOrNull
import checkme.web.tasks.forms.TaskClientResponse
import checkme.web.tasks.forms.TaskClientResponse.Companion.toClientEntryAnswerFormat
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import org.http4k.core.*
import org.http4k.lens.RequestContextLens

class ChangeTaskActualityHandler(
    private val tasksOperations: TaskOperationsHolder,
    private val userLens: RequestContextLens<User?>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)
        val taskId = request.idOrNull()
            ?: return objectMapper.sendBadRequestError(ChangeTaskActualityError.NO_ID_FOR_TASK.errorText)
        return when {
            user?.isAdmin() == true ->
                when (
                    val taskToUpdateActuality = fetchTask(
                        taskId = taskId,
                        taskOperations = tasksOperations
                    )
                ) {
                    is Failure -> objectMapper.sendBadRequestError(ChangeTaskActualityError.NO_SUCH_TASK.errorText)
                    is Success -> tryUpdateTaskActuality(
                        taskToUpdateActuality = taskToUpdateActuality.value,
                        tasksOperations = tasksOperations,
                        objectMapper = objectMapper
                    )
                }

            else -> objectMapper.sendBadRequestError(ChangeTaskActualityError.USER_HAS_NOT_RIGHTS.errorText)
        }
    }
}

private fun tryUpdateTaskActuality(
    taskToUpdateActuality: Task,
    tasksOperations: TaskOperationsHolder,
    objectMapper: ObjectMapper,
): Response {
    val actuality: Boolean = !taskToUpdateActuality.isActual
    return when (
        val updatedTask = changeTaskActuality(
            taskToUpdateActuality.copy(isActual = actuality),
            tasksOperations
        )
    ) {
        is Failure -> objectMapper.sendBadRequestError(
            ChangeTaskActualityError.UNKNOWN_DATABASE_ERROR.errorText
        )

        is Success -> objectMapper.sendOKResponse(
            TaskClientResponse(
                updatedTask.value.id,
                updatedTask.value.name,
                updatedTask.value.criterions,
                updatedTask.value.answerFormat.toClientEntryAnswerFormat(),
                updatedTask.value.description
            )
        )
    }
}

internal fun changeTaskActuality(
    task: Task,
    taskOperations: TaskOperationsHolder,
): Result<Task, ChangeTaskActualityError> {
    return when (
        val deletedTask = taskOperations.updateTaskActuality(task)
    ) {
        is Success -> Success(deletedTask.value)
        is Failure -> when (deletedTask.reason) {
            ModifyTaskError.UNKNOWN_DATABASE_ERROR -> Failure(ChangeTaskActualityError.UNKNOWN_DATABASE_ERROR)
        }
    }
}

enum class ChangeTaskActualityError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Something happened. Please try again later or ask for help"),
    NO_SUCH_TASK("The task does not exist"),
    NO_ID_FOR_TASK("No task id for change actuality"),
    USER_HAS_NOT_RIGHTS("Not allowed to change task actuality"),
}
