package checkme.web.solution.handlers

import checkme.domain.accounts.Role
import checkme.domain.models.User
import checkme.domain.operations.checks.CheckOperationHolder
import checkme.web.lenses.TaskLenses.taskIdPathField
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.MultipartForm
import org.http4k.lens.MultipartFormFile
import org.http4k.lens.Validator
import org.http4k.lens.multipartForm

const val COMPLETE_TASK = 10
const val SOLUTIONS_DIR = "/solutions"
const val SOLUTION_DIR = "/solution"

class CheckSolutionHandler(
    private val checkOperations: CheckOperationHolder,
) : HttpHandler {
    @Suppress("LongMethod", "NestedBlockDepth")
    override fun invoke(request: Request): Response {
        val filesField = MultipartFormFile.multi.required("ans")
        val filesLens = Body.Companion.multipartForm(Validator.Feedback, filesField).toLens()

        val filesForm: MultipartForm = filesLens(request)
        if (filesForm.errors.isNotEmpty()) {
            return Response(Status.BAD_REQUEST)
        }
        val objectMapper = jacksonObjectMapper()
        val taskId = taskIdPathField(request)

        //todo получение id задания из пути, задания из бд
        // todo добавление auth.user в контекст запроса
        val user = User(1, "login", "name", "surname", "pass", Role.STUDENT)

        return when (val newCheck = createNewCheck(taskId, 1, checkOperations)) {
            is Failure -> Response(Status.INTERNAL_SERVER_ERROR)
                .body(objectMapper.writeValueAsString(mapOf("error" to newCheck.reason.errorText)))

            is Success -> {
                println("Проверка создалась")
                val answers = mutableListOf<String>()
                var index = 0
                for (field in filesForm.fields) {
                    println(field)
                    if (field.key == index.toString()) {
                        answers.add(field.value.toString())
                        index++
                    }
                }
                index = 0
                for (file in filesForm.files) {
                    if (file.value.first().filename == index.toString()) {
                        val pathToFile = tryAddFileToUserSolutionDirectory(
                            checkId = newCheck.value.id,
                            user = user,
                            file = file.value.first(),
                            taskName = task.name
                        )
                        answers.add(pathToFile)
                        index++
                    }
                    index++
                }
                val checksResult = checkStudentAnswer(
                    task = task,
                    checkId = newCheck.value.id,
                    user = user)
                if (checksResult == null) {
                    println("Фигня, а не результат")
                    setStatusError(newCheck.value, checkOperations)
                } else {
                    when (val updatedCheck = updateCheckResult(newCheck.value.id, checksResult, checkOperations)) {
                        is Failure -> Response(Status.INTERNAL_SERVER_ERROR)
                            .body(
                                objectMapper.writeValueAsString(
                                    mapOf("error" to updatedCheck.reason.errorText)
                                )
                            )

                        is Success -> setStatusChecked(updatedCheck.value, checkOperations)
                    }
                }
            }
        }
    }
}

enum class CreationCheckError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Что-то случилось. Пожалуйста, повторите попытку позднее или обратитесь за помощью"),
}

enum class ModifyingCheckError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Что-то случилось. Пожалуйста, повторите попытку позднее или обратитесь за помощью"),
}
