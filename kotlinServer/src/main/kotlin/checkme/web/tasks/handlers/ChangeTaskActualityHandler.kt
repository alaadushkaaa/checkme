package checkme.web.tasks.handlers

import checkme.domain.models.ResultType
import checkme.domain.models.Task
import checkme.domain.models.User
import checkme.domain.operations.tasks.ModifyTaskError
import checkme.domain.operations.tasks.TaskOperationsHolder
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.lenses.GeneralWebLenses.idOrNull
import checkme.web.lenses.GeneralWebLenses.resultTypeOrNull
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
        val resultType = request.resultTypeOrNull()?.let { ResultType.resultTypeFromCode(it) }
        val taskId = request.idOrNull()
        return when {
            taskId == null ->
                objectMapper.sendBadRequestError(ChangeTaskActualityError.NO_ID_FOR_TASK.errorText)

            resultType == null ->
                objectMapper.sendBadRequestError(ChangeTaskActualityError.NO_RESULT_TYPE_FOR_TASK.errorText)

            else -> {
                when {
                    user?.isAdmin() == true ->
                        when (
                            val taskToUpdateActuality = fetchTask(
                                taskId = taskId,
                                taskOperations = tasksOperations
                            )
                        ) {
                            is Failure ->
                                objectMapper.sendBadRequestError(ChangeTaskActualityError.NO_SUCH_TASK.errorText)

                            is Success -> tryUpdateTaskActuality(
                                taskToUpdateActuality = taskToUpdateActuality.value,
                                tasksOperations = tasksOperations,
                                objectMapper = objectMapper,
                                resultType = resultType
                            )
                        }

                    else -> objectMapper.sendBadRequestError(ChangeTaskActualityError.USER_HAS_NOT_RIGHTS.errorText)
                }
            }
        }
    }
}

private fun tryUpdateTaskActuality(
    taskToUpdateActuality: Task,
    tasksOperations: TaskOperationsHolder,
    objectMapper: ObjectMapper,
    resultType: ResultType,
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

        is Success -> {
            when (resultType) {
                ResultType.TASK -> objectMapper.sendOKResponse(
                    TaskClientResponse(
                        updatedTask.value.id,
                        updatedTask.value.name,
                        updatedTask.value.criterions,
                        updatedTask.value.answerFormat.toClientEntryAnswerFormat(),
                        updatedTask.value.description
                    )
                )

                ResultType.LIST -> tryFetchTasks(
                    taskOperations = tasksOperations,
                    objectMapper = objectMapper
                )

                ResultType.HIDDEN -> tryFetchHiddenTasks(
                    taskOperations = tasksOperations,
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
        val tasks = fetchAllTasks(taskOperations)
    ) {
        is Failure -> objectMapper.sendBadRequestError(tasks.reason)
        is Success -> objectMapper.sendOKResponse(tasks.value)
    }
}

private fun tryFetchHiddenTasks(
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

private fun changeTaskActuality(
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
    NO_RESULT_TYPE_FOR_TASK("No result type for change actuality"),
    USER_HAS_NOT_RIGHTS("Not allowed to change task actuality"),
}
