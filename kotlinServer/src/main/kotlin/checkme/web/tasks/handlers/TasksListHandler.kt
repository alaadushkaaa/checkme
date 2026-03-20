package checkme.web.tasks.handlers

import checkme.domain.models.TaskWithBundles
import checkme.domain.models.User
import checkme.domain.operations.bundles.BundleOperationHolder
import checkme.domain.operations.tasks.TaskOperationsHolder
import checkme.web.bundles.handlers.selectTaskBundles
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.solution.handlers.ViewCheckResultError
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.*
import org.http4k.lens.RequestContextLens

class TasksListHandler(
    private val taskOperations: TaskOperationsHolder,
    private val bundleOperations: BundleOperationHolder,
    private val userLens: RequestContextLens<User?>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)
        return when {
            user == null -> objectMapper.sendBadRequestError(ViewCheckResultError.USER_HAS_NOT_RIGHTS)

            else -> {
                tryFetchTasksAndGroups(
                    bundleOperations = bundleOperations,
                    taskOperations = taskOperations,
                    objectMapper = objectMapper
                )
            }
        }
    }
}

private fun tryFetchTasksAndGroups(
    bundleOperations: BundleOperationHolder,
    taskOperations: TaskOperationsHolder,
    objectMapper: ObjectMapper,
): Response {
    return when (
        val tasks = fetchAllTasks(taskOperations)
    ) {
        is Failure -> objectMapper.sendBadRequestError(tasks.reason.errorText)
        is Success -> {
            val tasksAndGroups = mutableListOf<TaskWithBundles>()
            for (task in tasks.value) {
                when (
                    val taskBundles = selectTaskBundles(task.id, bundleOperations)
                ) {
                    is Failure -> objectMapper.sendBadRequestError(taskBundles.reason.errorText)
                    is Success -> tasksAndGroups.add(TaskWithBundles(task, taskBundles.value))
                }
            }
            objectMapper.sendOKResponse(tasksAndGroups)
        }
    }
}
