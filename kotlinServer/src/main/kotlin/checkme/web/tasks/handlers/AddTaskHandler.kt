package checkme.web.tasks.handlers

import checkme.domain.models.Task
import checkme.domain.operations.tasks.TaskOperationsHolder
import checkme.web.lenses.GeneralWebLenses.idOrNull
import checkme.web.lenses.TaskLenses
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.*
import org.http4k.lens.MultipartForm

const val TASKS_DIR = "/tasks"

class AddTaskHandler(
    private val tasksOperations: TaskOperationsHolder,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val taskId = request.idOrNull()
        val form: MultipartForm = TaskLenses.multipartFormFieldsAll(request)
        return when (
            val validatedNewTask = form.validateForm(taskId)
        ) {
            is Failure -> Response(Status.BAD_REQUEST).body(
                objectMapper.writeValueAsString(
                    mapOf("error" to validatedNewTask.reason.errorText)
                )
            )

            is Success -> {
                tryAddTaskAndFiles(
                    validatedNewTask = validatedNewTask.value,
                    taskOperations = tasksOperations,
                    objectMapper = objectMapper,
                    form = form
                )
            }
        }
    }
}

private fun tryAddTaskAndFiles(
    validatedNewTask: Task,
    taskOperations: TaskOperationsHolder,
    objectMapper: ObjectMapper,
    form: MultipartForm,
): Response {
    val updatedCriterions = validatedNewTask.addTaskFilesToDirectory(
        files = form.files,
        fields = form.fields,
        criterions = validatedNewTask.criterions
    )
    return when (
        val newTask =
            addTask(
                task = validatedNewTask.copy(criterions = updatedCriterions),
                taskOperations = taskOperations
            )
    ) {
        is Success -> {
            Response(Status.OK).body(
                objectMapper.writeValueAsString(
                    mapOf("taskId" to newTask.value.id)
                )
            )
        }

        is Failure -> Response(Status.BAD_REQUEST).body(
            objectMapper.writeValueAsString(
                mapOf("error" to newTask.reason.errorText)
            )
        )
    }
}
