package checkme.web.bundles.handlers

import checkme.domain.models.Task
import checkme.domain.models.TaskAndOrder
import checkme.domain.models.User
import checkme.domain.operations.bundles.BundleOperationHolder
import checkme.domain.operations.tasks.TaskOperationsHolder
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.lenses.GeneralWebLenses.idOrNull
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.lens.RequestContextLens

class SelectBundleTasks(
    private val userLens: RequestContextLens<User?>,
    private val taskOperations: TaskOperationsHolder,
    private val bundleOperations: BundleOperationHolder,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)
        val bundleId = request.idOrNull()
        val selectedTasksIds = objectMapper.readValue<List<String>>(request.bodyString())
        println(selectedTasksIds)
        return when {
            user == null || !user.isAdmin() ->
                objectMapper.sendBadRequestError(ViewSelectedTasksError.USER_CANT_VIEW_THIS_BUNDLE.errorText)

            bundleId == null ->
                objectMapper.sendBadRequestError(ViewSelectedTasksError.NO_BUNDLE_ID_ERROR.errorText)

            selectedTasksIds.isEmpty() ->
                objectMapper.sendBadRequestError(ViewSelectedTasksError.TASKS_IDS_LIST_IS_EMPTY_ERROR.errorText)

            else -> tryFetchSelectedTasks(
                tasksIds = selectedTasksIds,
                bundleId = bundleId,
                objectMapper = objectMapper,
                taskOperations = taskOperations,
                bundleOperations = bundleOperations
            )
        }
    }

    private fun tryFetchSelectedTasks(
        tasksIds: List<String>,
        bundleId: Int,
        objectMapper: ObjectMapper,
        taskOperations: TaskOperationsHolder,
        bundleOperations: BundleOperationHolder,
    ): Response {
        val fetchedTasks = mutableListOf<Task>()
        for (id in tasksIds) {
            when (val task = taskOperations.fetchTaskById(id.toInt())) {
                is Success -> fetchedTasks.add(task.value)
                is Failure -> {
                    return objectMapper.sendBadRequestError(
                        ViewSelectedTasksError.SELECT_TASK_ERROR.errorText
                    )
                }
            }
        }
        return when (
            val insertedTasks = tryUpdateBundleTasks(
                tasksAndOrder = fetchedTasks.map { TaskAndOrder(it, fetchedTasks.indexOf(it) + 1) },
                bundleId = bundleId,
                bundleOperations = bundleOperations
            )
        ) {
            is Failure -> objectMapper.sendBadRequestError(
                insertedTasks.reason.errorText
            )

            is Success -> objectMapper.sendOKResponse(insertedTasks.value).also {
            }
        }
    }
}

enum class ViewSelectedTasksError(val errorText: String) {
    NO_BUNDLE_ID_ERROR("No bundle id to view bundle info"),
    USER_CANT_VIEW_THIS_BUNDLE("User can't view this task"),
    TASKS_IDS_LIST_IS_EMPTY_ERROR("List with tasks ids can not be empty"),
    SELECT_TASK_ERROR("Something wrong while trying to fetch tasks. Please ask for help or try later"),
}
