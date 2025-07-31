package checkme.web.solution.handlers

import checkme.domain.models.User
import checkme.domain.operations.checks.CheckOperationHolder
import checkme.web.lenses.GeneralWebLenses.pageCountOrNull
import checkme.web.solution.supportingFiles.fetchAllChecksWithData
import checkme.web.solution.supportingFiles.fetchAllUsersChecksWithTaskData
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.*
import org.http4k.lens.RequestContextLens

class ListResultsHandler(
    private val checkOperations: CheckOperationHolder,
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
                        checkOperations = checkOperations
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
): Response {
    return when (
        val checksWithData =
            fetchAllChecksWithData(
                checkOperations = checkOperations,
                page = page
            )
    ) {
        is Failure -> Response(Status.BAD_REQUEST)
            .body(objectMapper.writeValueAsString(mapOf("error" to checksWithData.reason.errorText)))

        is Success -> Response(Status.OK)
            .body(objectMapper.writeValueAsString(checksWithData.value))
    }
}

private fun tryFetchUserSolutions(
    userId: Int,
    objectMapper: ObjectMapper,
    checkOperations: CheckOperationHolder,
): Response {
    return when (
        val usersChecks =
            fetchAllUsersChecksWithTaskData(
                checkOperations = checkOperations,
                userId = userId
            )
    ) {
        is Failure -> Response(Status.BAD_REQUEST)
            .body(objectMapper.writeValueAsString(mapOf("error" to usersChecks.reason.errorText)))

        is Success -> Response(Status.OK)
            .body(objectMapper.writeValueAsString(usersChecks.value))
    }
}
