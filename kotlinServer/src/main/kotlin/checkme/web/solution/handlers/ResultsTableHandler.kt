package checkme.web.solution.handlers

import checkme.domain.models.Check
import checkme.domain.models.User
import checkme.domain.operations.checks.CheckOperationHolder
import checkme.domain.operations.tasks.TaskOperationsHolder
import checkme.domain.operations.users.UserOperationHolder
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.solution.forms.TableSolutionsResponse
import checkme.web.solution.forms.UserDataForUsersList
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.valueOrNull
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.lens.RequestContextLens

class ResultsTableHandler(
    private val checkOperations: CheckOperationHolder,
    private val taskOperations: TaskOperationsHolder,
    private val userOperations: UserOperationHolder,
    private val userLens: RequestContextLens<User?>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        objectMapper.registerModule(JavaTimeModule())
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT)
        val user = userLens(request)
        return when {
            user == null || !user.isAdmin() ->
                objectMapper
                    .sendBadRequestError(ResultsTableError.USER_HAS_NOT_RIGHTS.errorText)

            else -> tryGetSolutionsWithData(
                taskOperations = taskOperations,
                userOperations = userOperations,
                checkOperations = checkOperations,
                objectMapper = objectMapper
            )
        }
    }
}

private fun tryGetSolutionsWithData(
    taskOperations: TaskOperationsHolder,
    userOperations: UserOperationHolder,
    checkOperations: CheckOperationHolder,
    objectMapper: ObjectMapper,
): Response {
    val tasksData = taskOperations.fetchAllTasksIdAndName()
    val usersData = userOperations.fetchUsersDataWithoutPassword()
    val solutionsData = checkOperations.fetchAllChecks()
    return when {
        tasksData is Failure ->
            objectMapper.sendBadRequestError(ResultsTableError.FETCH_TASKS_ERROR.errorText)

        usersData is Failure ->
            objectMapper.sendBadRequestError(ResultsTableError.FETCH_USER_DATA_ERROR.errorText)

        solutionsData is Failure ->
            objectMapper.sendBadRequestError(ResultsTableError.FETCH_SOLUTIONS_ERROR.errorText)

        else -> {
            val usersAndSolutions: Map<UserDataForUsersList, Check> =
                solutionsData.valueOrNull()?.mapNotNull { solution ->
                    usersData.valueOrNull()?.find { it.id == solution.userId.toString() }
                        ?.let { userData -> userData to solution }
                }?.toMap() ?: emptyMap()
            objectMapper.sendOKResponse(
                TableSolutionsResponse(
                    tasksData.valueOrNull() ?: emptyList(),
                    usersAndSolutions
                )
            )
        }
    }
}

enum class ResultsTableError(val errorText: String) {
    USER_HAS_NOT_RIGHTS("Not allowed to see this page"),
    FETCH_TASKS_ERROR("Something was wrong until tasks fetching. Please try again later."),
    FETCH_USER_DATA_ERROR("Something was wrong until user data fetching. Please try again later."),
    FETCH_SOLUTIONS_ERROR("Something was wrong until solutions fetching. Please try again later."),
}
