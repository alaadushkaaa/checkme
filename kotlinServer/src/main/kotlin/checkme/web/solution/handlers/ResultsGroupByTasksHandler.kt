package checkme.web.solution.handlers

import checkme.domain.models.User
import checkme.domain.operations.checks.CheckOperationHolder
import checkme.domain.operations.tasks.TaskOperationsHolder
import checkme.domain.operations.users.UserOperationHolder
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.lenses.GeneralWebLenses.pageCountOrNull
import checkme.web.solution.forms.CheckForTasksSolutions
import checkme.web.solution.forms.SolutionsGroupByTask
import checkme.web.solution.supportingFiles.fetchCheckByTaskId
import checkme.web.solution.supportingFiles.task
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.valueOrNull
import org.http4k.core.*
import org.http4k.lens.RequestContextLens

class ResultsGroupByTasksHandler(
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
            page == null ->
                objectMapper
                    .sendBadRequestError(ViewCheckResultError.NO_TASK_ID_ERROR.errorText)

            user?.isAdmin() == false ->
                objectMapper
                    .sendBadRequestError(ViewCheckResultError.USER_HAS_NOT_RIGHTS.errorText)

            else -> tryGetSolutionsGroupedByTask(
                userOperations = userOperations,
                taskOperations = taskOperations,
                checkOperations = checkOperations,
                page = page,
                objectMapper = objectMapper
            )
        }
    }
}

@Suppress("ReturnCount")
private fun tryGetSolutionsGroupedByTask(
    userOperations: UserOperationHolder,
    taskOperations: TaskOperationsHolder,
    checkOperations: CheckOperationHolder,
    page: Int,
    objectMapper: ObjectMapper,
): Response {
    val tasks = taskOperations.fetchAllTasksPagination(page)
    val tasksAndSolutions: List<SolutionsGroupByTask> = tasks.valueOrNull()
        ?.map { task ->
            when (val taskChecks = fetchCheckByTaskId(task.id, checkOperations)) {
                is Failure ->
                    return objectMapper.sendBadRequestError(taskChecks.reason)

                is Success -> {
                    val solutions = taskChecks.value.map { check ->
                        val userForCheck = userOperations.fetchUserNameSurname(check.userId)
                        when (userForCheck) {
                            is Failure ->
                                return objectMapper.sendBadRequestError(userForCheck.reason)

                            is Success -> {
                                CheckForTasksSolutions(
                                    id = check.id.toString(),
                                    date = check.date.toString(),
                                    status = check.status,
                                    result = check.result,
                                    user = userForCheck.value,
                                    totalScore = check.totalScore
                                )
                            }
                        }
                    }
                    SolutionsGroupByTask(
                        task = task,
                        solutions = solutions
                    )
                }
            }
        } ?: emptyList()
    return objectMapper.sendOKResponse(tasksAndSolutions)
}
