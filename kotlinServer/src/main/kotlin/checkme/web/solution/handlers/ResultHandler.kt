package checkme.web.solution.handlers

import checkme.domain.models.Check
import checkme.domain.models.User
import checkme.domain.operations.checks.CheckOperationHolder
import checkme.domain.operations.tasks.TaskOperationsHolder
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.lenses.GeneralWebLenses.checkIdOrNull
import checkme.web.solution.forms.ResultResponse
import checkme.web.solution.forms.TaskResultResponse
import checkme.web.solution.supportingFiles.fetchCheckById
import checkme.web.tasks.handlers.fetchTask
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.*
import org.http4k.lens.RequestContextLens
import java.util.UUID

class ResultHandler(
    private val checkOperations: CheckOperationHolder,
    private val taskOperations: TaskOperationsHolder,
    private val userLens: RequestContextLens<User?>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)
        val checkId = request.checkIdOrNull()
            ?: return objectMapper.sendBadRequestError(ViewCheckResultError.NO_CHECK_ID_ERROR.errorText)

        return when {
            user == null -> objectMapper.sendBadRequestError(ViewCheckResultError.USER_HAS_NOT_RIGHTS)

            else ->
                tryFetchCheckResultData(
                    taskOperations = taskOperations,
                    checkOperations = checkOperations,
                    objectMapper = objectMapper,
                    user = user,
                    checkId = checkId
                )
        }
    }
}

private fun tryFetchCheckResultData(
    taskOperations: TaskOperationsHolder,
    checkOperations: CheckOperationHolder,
    objectMapper: ObjectMapper,
    user: User,
    checkId: UUID,
): Response {
    return when (
        val check = fetchCheckById(
            checkId = checkId,
            checkOperations = checkOperations
        )
    ) {
        is Failure -> objectMapper.sendBadRequestError(check.reason.errorText)

        is Success -> {
            if (!user.isAdmin() && user.id != check.value.userId) {
                objectMapper.sendBadRequestError(
                    ViewCheckResultError.USER_HAS_NOT_RIGHTS
                )
            } else {
                tryFetchTaskAndSendResponse(
                    taskOperations = taskOperations,
                    objectMapper = objectMapper,
                    check = check.value
                )
            }
        }
    }
}

private fun tryFetchTaskAndSendResponse(
    taskOperations: TaskOperationsHolder,
    objectMapper: ObjectMapper,
    check: Check,
): Response {
    return when (
        val fetchedTaskForResult = fetchTask(
            taskId = check.taskId,
            taskOperations = taskOperations
        )
    ) {
        is Failure -> objectMapper.sendBadRequestError(fetchedTaskForResult.reason.errorText)

        is Success -> objectMapper.sendOKResponse(
            ResultResponse(
                status = check.status,
                result = check.result,
                task = TaskResultResponse(
                    id = fetchedTaskForResult.value.id.toString(),
                    name = fetchedTaskForResult.value.name
                ),
                totalScore = check.totalScore
            )
        )
    }
}

enum class ViewCheckResultError(val errorText: String) {
    NO_CHECK_ID_ERROR("No check id for result"),
    USER_HAS_NOT_RIGHTS("You has not rights to see this result"),
    NO_USER_ID_ERROR("No user id for check list"),
    NO_TASK_ID_ERROR("No task id for check list"),
    NO_PAGE_ERROR("No page to show check list"),
}

enum class FetchingCheckError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Something happened. Please try again later or ask for help"),
    NO_CHECK_IN_DB("Task for check does not exists"),
}
