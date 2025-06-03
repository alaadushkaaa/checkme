package checkme.web.solution.handlers

import checkme.domain.accounts.Role
import checkme.domain.models.User
import checkme.domain.operations.checks.CheckOperationHolder
import checkme.web.lenses.TaskLenses.taskIdPathField
import checkme.web.task
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.*
import org.http4k.lens.*

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

        // todo добавление auth.user в контекст запроса
        val user = User(1, "login", "name", "surname", "pass", Role.STUDENT)

        return when (val newCheck = createNewCheck(taskId, 1, checkOperations)) {
            is Failure -> Response(Status.INTERNAL_SERVER_ERROR)
                .body(objectMapper.writeValueAsString(mapOf("error" to newCheck.reason.errorText)))

            is Success -> {
                val answers = mutableListOf<String>()
                var index = 0
                for (field in filesForm.fields) {
                    if (field.key == index.toString()) {
                        answers.add(field.value.toString())
                        index++
                    } else {
                        if (filesForm.files.containsKey(index.toString())) {
                            val file = filesForm.files[index.toString()]?.first()

                            if (file != null) {
                                val pathToFile = tryAddFileToUserSolutionDirectory(
                                    checkId = newCheck.value.id,
                                    user = user,
                                    file
                                )
                                answers.add(pathToFile)
                            }
                            index++
                        }
                    }
                }
                val checksResult = checkStudentAnswer(task, newCheck.value.id)
                if (checksResult == null) {
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
