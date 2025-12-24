package checkme.web.solution.handlers

import checkme.domain.models.User
import checkme.domain.operations.checks.CheckOperationHolder
import checkme.domain.operations.tasks.TaskOperationsHolder
import checkme.domain.operations.users.UserOperationHolder
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.lenses.GeneralWebLenses.idOrNull
import checkme.web.solution.forms.ChecksForUsersSolutions
import checkme.web.solution.forms.ListUserCheck
import checkme.web.solution.supportingFiles.fetchCheckByUserId
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.valueOrNull
import org.http4k.core.*
import org.http4k.lens.RequestContextLens

class ListUserResultsHandler(
    private val checkOperations: CheckOperationHolder,
    private val taskOperations: TaskOperationsHolder,
    private val userOperations: UserOperationHolder,
    private val userLens: RequestContextLens<User?>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)
        val userId = request.idOrNull() ?: return objectMapper
            .sendBadRequestError(ViewCheckResultError.NO_USER_ID_ERROR.errorText)
        return when {
            user == null || !user.isAdmin() ->
                objectMapper
                    .sendBadRequestError(ViewCheckResultError.USER_HAS_NOT_RIGHTS.errorText)

            else -> {
                val userForChecks = userOperations.fetchUserById(userId)
                if (userForChecks is Failure) {
                    objectMapper.sendBadRequestError()
                } else {
                    tryFetchUserSolutions(
                        user = userForChecks.valueOrNull()!!,
                        objectMapper = objectMapper,
                        checkOperations = checkOperations,
                        taskOperations = taskOperations
                    )
                }
            }
        }
    }
}

private fun tryFetchUserSolutions(
    user: User,
    objectMapper: ObjectMapper,
    checkOperations: CheckOperationHolder,
    taskOperations: TaskOperationsHolder,
): Response {
    return when (
        val userChecks = fetchCheckByUserId(
            userId = user.id,
            checkOperations = checkOperations
        )
    ) {
        is Failure -> objectMapper.sendBadRequestError(userChecks.reason.errorText)

        is Success -> {
            val checks = userChecks.value.map { check ->
                val taskData = taskOperations.fetchTaskName(check.taskId)
                if (taskData is Failure) objectMapper.sendBadRequestError()
                ChecksForUsersSolutions(
                    id = check.id.toString(),
                    date = check.date.toString(),
                    status = check.status,
                    result = check.result,
                    task = taskData.valueOrNull()!!,
                    totalScore = check.totalScore
                )
            }
            objectMapper.sendOKResponse(
                ListUserCheck(
                    name = user.name,
                    surname = user.surname,
                    solutions = checks
                )
            )
        }
    }
}
