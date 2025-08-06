package checkme.web.solution.handlers

import checkme.domain.models.User
import checkme.domain.operations.checks.CheckOperationHolder
import checkme.domain.operations.tasks.TaskOperationsHolder
import checkme.domain.operations.users.UserOperationHolder
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKRequest
import checkme.web.lenses.GeneralWebLenses.pageCountOrNull
import checkme.web.solution.forms.CheckWithAllData
import checkme.web.solution.forms.CheckWithTaskData
import checkme.web.solution.supportingFiles.fetchAllChecksPagination
import checkme.web.solution.supportingFiles.fetchCheckByUserId
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.valueOrNull
import org.http4k.core.*
import org.http4k.lens.RequestContextLens

class ListResultsHandler(
    private val checkOperations: CheckOperationHolder,
    private val taskOperations: TaskOperationsHolder,
    private val userOperations: UserOperationHolder,
    private val userLens: RequestContextLens<User?>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        // todo пока реализована возможность просмотра админом общих результатов и
        // каждого пользователя своих
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)
        val page = request.pageCountOrNull()
        return when {
            user == null -> objectMapper.sendBadRequestError(ViewCheckResultError.USER_HAS_NOT_RIGHTS)

            user.isAdmin() && page != null ->
                tryFetchAllSolutionsByAdmin(
                    page = page,
                    objectMapper = objectMapper,
                    checkOperations = checkOperations,
                    userOperations = userOperations,
                    taskOperations = taskOperations
                )

            else -> tryFetchUserSolutions(
                userId = user.id,
                objectMapper = objectMapper,
                checkOperations = checkOperations,
                taskOperations = taskOperations
            )
        }
    }
}

private fun tryFetchUserSolutions(
    userId: Int,
    objectMapper: ObjectMapper,
    checkOperations: CheckOperationHolder,
    taskOperations: TaskOperationsHolder,
): Response {
    return when (
        val userChecks = fetchCheckByUserId(
            userId = userId,
            checkOperations = checkOperations
        )
    ) {
        is Failure -> objectMapper.sendBadRequestError(userChecks.reason.errorText)

        is Success -> {
            val checksWithTaskData = userChecks.value.map { check ->
                val taskData = taskOperations.fetchTaskName(check.taskId)
                if (taskData is Failure) objectMapper.sendBadRequestError()
                CheckWithTaskData(
                    id = check.id.toString(),
                    date = check.date.toString(),
                    status = check.status,
                    task = taskData.valueOrNull()!!
                )
            }
            objectMapper.sendOKRequest(checksWithTaskData)
        }
    }
}

private fun tryFetchAllSolutionsByAdmin(
    page: Int,
    objectMapper: ObjectMapper,
    checkOperations: CheckOperationHolder,
    taskOperations: TaskOperationsHolder,
    userOperations: UserOperationHolder,
): Response {
    return when (
        val checksWithData =
            fetchAllChecksPagination(
                checkOperations = checkOperations,
                page = page
            )
    ) {
        is Failure -> objectMapper.sendBadRequestError(mapOf("error" to checksWithData.reason.errorText))

        is Success -> {
            val checksWithAllData = checksWithData.value.map { check ->
                val userData = userOperations.fetchUserNameSurname(check.userId)
                if (userData is Failure) objectMapper.sendBadRequestError()
                val taskData = taskOperations.fetchTaskName(check.taskId)
                if (taskData is Failure) objectMapper.sendBadRequestError()
                CheckWithAllData(
                    id = check.id.toString(),
                    status = check.status,
                    date = check.date.toString(),
                    result = check.result,
                    user = userData.valueOrNull()!!,
                    task = taskData.valueOrNull()!!
                )
            }
            objectMapper.sendOKRequest(checksWithAllData)
        }
    }
}
