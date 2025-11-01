package checkme.web.tasks.handlers

import checkme.domain.models.User
import checkme.domain.operations.tasks.TaskOperationsHolder
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.lenses.GeneralWebLenses.idOrNull
import checkme.web.tasks.forms.TaskClientResponse
import checkme.web.tasks.forms.TaskClientResponse.Companion.toClientEntryAnswerFormat
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.*
import org.http4k.lens.RequestContextLens

class TaskHandler(
    private val userLens: RequestContextLens<User?>,
    private val taskOperations: TaskOperationsHolder,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)
        val taskId = request.idOrNull()
        return when {
            user == null -> objectMapper.sendBadRequestError(ViewTaskError.USER_CANT_VIEW_THIS_TASK.errorText)
            taskId == null -> objectMapper.sendBadRequestError(ViewTaskError.NO_TASK_ID_ERROR.errorText)
            else -> tryFetchTask(
                taskId = taskId,
                objectMapper = objectMapper,
                taskOperations = taskOperations,
                user = user
            )
        }
    }
}

private fun tryFetchTask(
    taskId: Int,
    objectMapper: ObjectMapper,
    user: User,
    taskOperations: TaskOperationsHolder,
): Response {
    return when (val task = fetchTask(taskId, taskOperations)) {
        is Failure -> return objectMapper.sendBadRequestError(task.reason.errorText)

        is Success -> {
            if (!task.value.isActual && !user.isAdmin()) {
                objectMapper.sendBadRequestError(ViewTaskError.USER_CANT_VIEW_THIS_TASK.errorText)
            } else {
                objectMapper.sendOKResponse(
                    TaskClientResponse(
                        task.value.id,
                        task.value.name,
                        task.value.criterions,
                        task.value.answerFormat.toClientEntryAnswerFormat(),
                        task.value.description,
                        task.value.isActual
                    )
                )
            }
        }
    }
}

enum class ViewTaskError(val errorText: String) {
    NO_TASK_ID_ERROR("The ID of the task to view is missing"),
    USER_CANT_VIEW_THIS_TASK("User can't view this task"),
}
