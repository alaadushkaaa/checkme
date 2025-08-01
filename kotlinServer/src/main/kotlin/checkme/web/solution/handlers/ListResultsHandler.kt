package checkme.web.solution.handlers

import checkme.domain.models.User
import checkme.domain.operations.checks.CheckOperationHolder
import checkme.domain.operations.tasks.TaskOperationsHolder
import checkme.domain.operations.users.UserOperationHolder
import checkme.web.lenses.GeneralWebLenses.pageCountOrNull
import checkme.web.solution.forms.CheckDataForAllResults
import checkme.web.solution.forms.CheckWithAllData
import checkme.web.solution.supportingFiles.fetchAllChecksDateStatus
import checkme.web.solution.supportingFiles.fetchAllChecksWithData
import checkme.web.solution.supportingFiles.fetchAllUsersChecksWithTaskData
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.map
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
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)
        val page = request.pageCountOrNull()
        return when {
            user == null -> Response(Status.BAD_REQUEST)
                .body(objectMapper.writeValueAsString(mapOf("error" to ViewCheckResultError.USER_HAS_NOT_RIGHTS)))

            else -> {
                if (user.isAdmin() && page != null) {
                    tryFetchAllSolutionsByAdmin(
                        page = page,
                        objectMapper = objectMapper,
                        checkOperations = checkOperations,
                        userOperations = userOperations,
                        taskOperations = taskOperations
                    )
                } else {
                    tryFetchUserSolutions(
                        userId = user.id,
                        objectMapper = objectMapper,
                        checkOperations = checkOperations
                    )
                }
            }
        }
    }
}

private fun tryFetchAllSolutionsByAdmin(
    page: Int,
    objectMapper: ObjectMapper,
    checkOperations: CheckOperationHolder,
    taskOperations: TaskOperationsHolder,
    userOperations: UserOperationHolder
): Response {
    return when (
        val checksWithData =
            fetchAllChecksDateStatus(
                checkOperations = checkOperations,
                page = page
            )
    ) {
        is Failure -> Response(Status.BAD_REQUEST)
            .body(objectMapper.writeValueAsString(mapOf("error" to checksWithData.reason.errorText)))

        is Success -> {
            val checksWithAllData = checksWithData.value.map { check -> {
                val userData = userOperations.fetchUserNAmeSurname(check.userId)
                if (userData is Failure) Response(Status.BAD_REQUEST)
                    .body(objectMapper.writeValueAsString(mapOf("error" to FetchingCheckError.UNKNOWN_DATABASE_ERROR)))
                val taskData = taskOperations.fetchTaskName(check.taskId)
                if (taskData is Failure) Response(Status.BAD_REQUEST)
                    .body(objectMapper.writeValueAsString(mapOf("error" to FetchingCheckError.UNKNOWN_DATABASE_ERROR)))
                CheckWithAllData(
                    id = check.id,
                    status = check.status,
                    date = check.date,
                    userData = userData.valueOrNull()!!,
                    taskData = taskData.valueOrNull()!!
                )
            } }
            Response(Status.OK)
                .body(objectMapper.writeValueAsString(checksWithAllData))
        }
    }
}

