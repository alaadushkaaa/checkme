package checkme.web.solution.handlers

import checkme.domain.models.Task
import checkme.domain.models.User
import checkme.domain.operations.checks.CheckOperationHolder
import checkme.domain.operations.tasks.TaskOperationsHolder
import checkme.domain.operations.users.UserOperationHolder
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.lenses.GeneralWebLenses.idOrNull
import checkme.web.solution.forms.CheckForTasksSolutions
import checkme.web.solution.forms.ListTaskCheck
import checkme.web.solution.supportingFiles.fetchCheckByTaskId
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.valueOrNull
import org.http4k.core.*
import org.http4k.lens.RequestContextLens

class ListTaskResultsHandler(
    private val checkOperations: CheckOperationHolder,
    private val taskOperations: TaskOperationsHolder,
    private val userOperations: UserOperationHolder,
    private val userLens: RequestContextLens<User?>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)
        val taskId = request.idOrNull() ?: return objectMapper
            .sendBadRequestError(ViewCheckResultError.NO_TASK_ID_ERROR.errorText)
        return when {
            user == null || !user.isAdmin() ->
                objectMapper
                    .sendBadRequestError(ViewCheckResultError.USER_HAS_NOT_RIGHTS.errorText)

            else -> {
                val taskForChecks = taskOperations.fetchTaskById(taskId)
                if (taskForChecks is Failure) {
                    objectMapper.sendBadRequestError()
                } else {
                    tryFetchTaskSolutions(
                        task = taskForChecks.valueOrNull()!!,
                        objectMapper = objectMapper,
                        checkOperations = checkOperations,
                        userOperations = userOperations
                    )
                }
            }
        }
    }
}

private fun tryFetchTaskSolutions(
    task: Task,
    objectMapper: ObjectMapper,
    checkOperations: CheckOperationHolder,
    userOperations: UserOperationHolder,
): Response {
    return when (
        val taskChecks = fetchCheckByTaskId(
            taskId = task.id,
            checkOperations = checkOperations
        )
    ) {
        is Failure -> objectMapper.sendBadRequestError(taskChecks.reason.errorText)

        is Success -> {
            val checks = taskChecks.value.map { check ->
                val userData = userOperations.fetchUserNameSurname(check.userId)
                if (userData is Failure) objectMapper.sendBadRequestError()
                CheckForTasksSolutions(
                    id = check.id.toString(),
                    date = check.date.toString(),
                    status = check.status,
                    result = check.result,
                    user = userData.valueOrNull()!!
                )
            }
            objectMapper.sendOKResponse(
                ListTaskCheck(
                    name = task.name,
                    solutions = checks
                )
            )
        }
    }
}
