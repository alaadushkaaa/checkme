package checkme.web.bundles.handlers

import checkme.domain.models.TaskAndOrder
import checkme.domain.models.User
import checkme.domain.operations.bundles.BundleOperationHolder
import checkme.domain.operations.tasks.TaskOperationsHolder
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.lenses.GeneralWebLenses.idOrNull
import checkme.web.solution.supportingFiles.task
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.lens.RequestContextLens

class ChangeBundleTasksOrderHandler(
    private val userLens: RequestContextLens<User?>,
    private val taskOperations: TaskOperationsHolder,
    private val bundleOperations: BundleOperationHolder
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)
        val bundleId = request.idOrNull()
        val selectedTasksAndOrder = objectMapper.readValue<List<TaskAndOrder>>(request.bodyString())
        println(selectedTasksAndOrder)
        return when {
            user == null || !user.isAdmin() ->
                objectMapper.sendBadRequestError(ViewSelectedTasksError.USER_CANT_VIEW_THIS_BUNDLE.errorText)
                    .also { println("000") }

            bundleId == null ->
                objectMapper.sendBadRequestError(ViewSelectedTasksError.NO_BUNDLE_ID_ERROR.errorText)
                    .also { println("111") }

            selectedTasksAndOrder.isEmpty() ->
                objectMapper.sendBadRequestError(ViewSelectedTasksError.TASKS_IDS_LIST_IS_EMPTY_ERROR.errorText)
                    .also { println("222") }

            else -> tryUpdateTasksOrders(
                tasksAndOrders = selectedTasksAndOrder,
                bundleId = bundleId,
                objectMapper = objectMapper,
                bundleOperations = bundleOperations
            )
        }
    }

    private fun tryUpdateTasksOrders(
        tasksAndOrders: List<TaskAndOrder>,
        bundleId: Int,
        objectMapper: ObjectMapper,
        bundleOperations: BundleOperationHolder
    ): Response {
        return when (
            val updatedTasksAndOrders = tryUpdateBundleTasks(
                tasksAndOrder = tasksAndOrders,
                bundleId = bundleId,
                bundleOperations = bundleOperations
            )
        ) {
            is Failure -> objectMapper.sendBadRequestError(
                updatedTasksAndOrders.reason.errorText
            )

            is Success -> objectMapper.sendOKResponse(updatedTasksAndOrders.value)
        }
    }
}