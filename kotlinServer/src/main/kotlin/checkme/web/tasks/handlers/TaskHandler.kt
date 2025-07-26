package checkme.web.tasks.handlers

import checkme.domain.operations.tasks.TaskOperationsHolder
import checkme.web.lenses.GeneralWebLenses.idOrNull
import checkme.web.tasks.forms.TaskClientResponse
import checkme.web.tasks.forms.TaskClientResponse.Companion.toClientEntryAnswerFormat
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.*

class TaskHandler(
    private val taskOperations: TaskOperationsHolder,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val taskId = request.idOrNull() ?: return Response(Status.BAD_REQUEST).body(
            objectMapper.writeValueAsString(
                mapOf("error" to ViewTaskError.NO_TASK_ID_ERROR.errorText)
            )
        )
        return tryFetchTask(
            taskId = taskId,
            objectMapper = objectMapper,
            taskOperations = taskOperations
        )
    }
}

private fun tryFetchTask(
    taskId: Int,
    objectMapper: ObjectMapper,
    taskOperations: TaskOperationsHolder,
): Response {
    return when (val task = fetchTask(taskId, taskOperations)) {
        is Failure -> return Response(Status.BAD_REQUEST).body(
            objectMapper.writeValueAsString(
                mapOf("error" to task.reason.errorText)
            )
        )
        is Success -> Response(Status.OK).body(
            objectMapper.writeValueAsString(
                TaskClientResponse(
                    task.value.id,
                    task.value.name,
                    task.value.criterions,
                    task.value.answerFormat.toClientEntryAnswerFormat(),
                    task.value.description
                )
            )
        )
    }
}

enum class ViewTaskError(val errorText: String) {
    NO_TASK_ID_ERROR("Отсутствует идентификатор задания для просмотра"),
}
