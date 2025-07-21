package checkme.web.solution.handlers

import checkme.domain.forms.CheckResult
import checkme.domain.models.Check
import checkme.domain.models.User
import checkme.domain.operations.checks.CheckOperationHolder
import checkme.domain.operations.tasks.TaskOperationsHolder
import checkme.domain.tools.TokenError
import checkme.web.lenses.TaskLenses.taskIdPathField
import com.fasterxml.jackson.databind.ObjectMapper
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
import org.http4k.lens.RequestContextLens
import org.http4k.lens.Validator
import org.http4k.lens.multipartForm

const val COMPLETE_TASK = 10
const val SOLUTIONS_DIR = "/solutions"
const val SOLUTION_DIR = "/solution"

class CheckSolutionHandler(
    private val checkOperations: CheckOperationHolder,
    private val taskOperations: TaskOperationsHolder,
    private val userLens: RequestContextLens<User?>,
) : HttpHandler {
    @Suppress("LongMethod", "NestedBlockDepth", "ReturnCount")
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)
        return when {
            user == null -> Response(Status.OK)
                .body(objectMapper.writeValueAsString(mapOf("error" to TokenError.DECODING_ERROR)))

            else -> {
                val filesField = MultipartFormFile.multi.required("ans")
                val filesLens = Body.Companion.multipartForm(Validator.Feedback, filesField).toLens()

                val filesForm: MultipartForm = filesLens(request)
                if (filesForm.errors.isNotEmpty()) {
                    return Response(Status.BAD_REQUEST)
                }
                val taskId = taskIdPathField(request)

                return when (
                    val newCheck = createNewCheck(
                        taskId = taskId,
                        userId = 1,
                        checkOperations = checkOperations,
                        taskOperations = taskOperations
                    )
                ) {
                    is Failure -> Response(Status.INTERNAL_SERVER_ERROR)
                        .body(objectMapper.writeValueAsString(mapOf("error" to newCheck.reason.errorText)))

                    is Success -> {
                        val answers = tryGetFieldsAndFilesFromForm(
                            filesForm = filesForm,
                            newCheck = newCheck.value,
                            user = user
                        )
                        val checksResult = Check.checkStudentAnswer(
                            task = task,
                            checkId = newCheck.value.id,
                            user = user,
                            answers = answers
                        )
                        sendResponseWithChecksResult(
                            checksResult,
                            newCheck.value,
                            objectMapper
                        )
                    }
                }
            }
        }
    }

    private fun sendResponseWithChecksResult(
        checksResult: Map<String, CheckResult>?,
        newCheck: Check,
        objectMapper: ObjectMapper,
    ): Response {
        return when {
            checksResult == null -> setStatusError(newCheck, checkOperations)
            else -> when (val updatedCheck = updateCheckResult(newCheck.id, checksResult, checkOperations)) {
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

enum class CreationCheckError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Something happened. Please try again later or ask for help"),
    NO_TASK_FOR_CHECK_IN_DB("Task for check does not exists"),
}

enum class ModifyingCheckError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Something happened. Please try again later or ask for help"),
}
