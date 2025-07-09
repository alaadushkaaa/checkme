package checkme.web.tasks.handlers

import checkme.domain.operations.tasks.TaskOperationsHolder
import checkme.web.lenses.GeneralWebLenses.idOrNull
import checkme.web.lenses.TaskLenses
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.*
import org.http4k.lens.MultipartForm

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
            is Failure -> Response(Status.INTERNAL_SERVER_ERROR).body(
                objectMapper.writeValueAsString(
                    mapOf("error" to validatedNewTask.reason)
                )
            )

            is Success -> {
                when (
                    val newTask =
                        addTask(
                            validatedNewTask.value,
                            tasksOperations
                        )
                ) {
                    is Success -> {
                        //todo перемещение файлов-проверок в директорию
                        //todo сохранять особые проверки!
                        Response(Status.OK).body(
                            objectMapper.writeValueAsString(
                                mapOf("taskId" to newTask.value.id)
                            )
                        )
                    }

                    is Failure -> Response(Status.INTERNAL_SERVER_ERROR).body(
                        objectMapper.writeValueAsString(
                            mapOf("error" to newTask.reason.errorText)
                        )
                    )
                }
            }
        }
    }
}
