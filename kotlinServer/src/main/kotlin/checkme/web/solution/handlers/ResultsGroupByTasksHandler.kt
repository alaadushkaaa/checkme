package checkme.web.solution.handlers

import checkme.domain.models.Check
import checkme.domain.models.Task
import checkme.domain.models.User
import checkme.domain.operations.checks.CheckOperationHolder
import checkme.domain.operations.tasks.TaskOperationsHolder
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.lenses.GeneralWebLenses.pageCountOrNull
import checkme.web.solution.supportingFiles.fetchCheckByTaskId
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.valueOrNull
import org.http4k.core.*
import org.http4k.lens.RequestContextLens

class ResultsGroupByTasksHandler(
    private val checkOperations: CheckOperationHolder,
    private val taskOperations: TaskOperationsHolder,
    private val userLens: RequestContextLens<User?>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)
        val page = request.pageCountOrNull()
        return when {
            page == null -> objectMapper
                .sendBadRequestError(ViewCheckResultError.NO_TASK_ID_ERROR.errorText)

            user?.isAdmin() == true ->
                objectMapper
                    .sendBadRequestError(ViewCheckResultError.USER_HAS_NOT_RIGHTS.errorText)

            else -> {
                val tasks = taskOperations.fetchAllTasksPagination(page)
                val tasksAndSolutions: Map<Task, List<Check>> = tasks.valueOrNull()
                    ?.associateWith { task ->
                        when (
                            val taskChecks = fetchCheckByTaskId(task.id, checkOperations)
                        ) {
                            is Failure ->
                                return objectMapper
                                    .sendBadRequestError(taskChecks.reason)

                            is Success -> taskChecks.value
                        }
                    } ?: emptyMap()
                objectMapper.sendOKResponse(tasksAndSolutions)
            }
        }
    }
}
