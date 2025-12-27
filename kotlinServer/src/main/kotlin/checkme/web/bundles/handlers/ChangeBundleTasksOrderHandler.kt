package checkme.web.bundles.handlers

import checkme.domain.models.TaskAndOrder
import checkme.domain.models.User
import checkme.domain.operations.bundles.BundleOperationHolder
import checkme.logging.LoggerType
import checkme.logging.ServerLogger
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

class ChangeBundleTasksOrderHandler(
    private val userLens: RequestContextLens<User?>,
    private val bundleOperations: BundleOperationHolder,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)
        val bundleId = request.idOrNull()
        val selectedTasksAndOrder = objectMapper.readValue<List<TaskAndOrder>>(request.bodyString())
        return when {
            user == null || !user.isAdmin() ->
                objectMapper.sendBadRequestError(ViewSelectedTasksError.USER_CANT_VIEW_THIS_BUNDLE.errorText)

            bundleId == null ->
                objectMapper.sendBadRequestError(ViewSelectedTasksError.NO_BUNDLE_ID_ERROR.errorText)

            selectedTasksAndOrder.isEmpty() ->
                objectMapper.sendBadRequestError(ViewSelectedTasksError.TASKS_IDS_LIST_IS_EMPTY_ERROR.errorText)

            else -> tryUpdateTasksOrders(
                user = user,
                tasksAndOrders = selectedTasksAndOrder,
                bundleId = bundleId,
                objectMapper = objectMapper,
                bundleOperations = bundleOperations
            )
        }
    }

    private fun tryUpdateTasksOrders(
        user: User,
        tasksAndOrders: List<TaskAndOrder>,
        bundleId: Int,
        objectMapper: ObjectMapper,
        bundleOperations: BundleOperationHolder,
    ): Response {
        return when (
            val updatedTasksAndOrders = tryUpdateBundleTasks(
                tasksAndOrder = tasksAndOrders,
                bundleId = bundleId,
                bundleOperations = bundleOperations
            )
        ) {
            is Failure -> {
                ServerLogger.log(
                    user = user,
                    action = "Change bundle task order error",
                    message = "Error: ${updatedTasksAndOrders.reason.errorText}",
                    type = LoggerType.INFO
                )
                objectMapper.sendBadRequestError(
                    updatedTasksAndOrders.reason.errorText
                )
            }

            is Success -> {
                ServerLogger.log(
                    user = user,
                    action = "Change bundle tasks order",
                    message = "Admin is changed bundle tasks order for bundle with id $bundleId",
                    type = LoggerType.INFO
                )
                objectMapper.sendOKResponse(updatedTasksAndOrders.value)
            }
        }
    }
}
