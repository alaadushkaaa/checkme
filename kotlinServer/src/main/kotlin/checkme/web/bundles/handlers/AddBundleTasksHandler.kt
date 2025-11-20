package checkme.web.bundles.handlers

import checkme.domain.models.TaskAndOrder
import checkme.domain.models.User
import checkme.domain.operations.bundles.BundleOperationHolder
import checkme.web.bundles.forms.BundleTasksResponse
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.lenses.GeneralWebLenses.idOrNull
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.*
import org.http4k.lens.RequestContextLens

class AddBundleTasksHandler(
    private val bundleOperations: BundleOperationHolder,
    private val userLens: RequestContextLens<User?>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)
        val bundleId = request.idOrNull()
        return when {
            user == null || !user.isAdmin() ->
                objectMapper.sendBadRequestError(AddBundleTasksError.USER_HAS_NOT_RIGHTS.errorText)

            bundleId == null ->
                objectMapper.sendBadRequestError(AddBundleTasksError.NO_BUNDLE_ID_TO_ADD_TASKS.errorText)

            else -> tryInsertOrUpdateBundleTasks(
                objectMapper = objectMapper,
                request = request,
                bundleId = bundleId
            )
        }
    }

    private fun tryInsertOrUpdateBundleTasks(
        objectMapper: ObjectMapper,
        request: Request,
        bundleId: Int,
    ): Response {
        val tasks = objectMapper.readValue<List<TaskAndOrder>>(request.bodyString())
        return when (val selectedTasks = selectBundleTasks(bundleId, bundleOperations)) {
            is Failure -> objectMapper.sendBadRequestError(AddBundleTasksError.UNKNOWN_DATABASE_ERROR.errorText)
            is Success -> {
                when {
                    selectedTasks.value.isNotEmpty() -> tryUpdateTasks(
                        tasks = tasks,
                        bundleId = bundleId,
                        objectMapper = objectMapper
                    )

                    else -> tryInsertTasks(
                        tasks = tasks,
                        bundleId = bundleId,
                        objectMapper = objectMapper
                    )
                }
            }
        }
    }

    private fun tryInsertTasks(
        tasks: List<TaskAndOrder>,
        bundleId: Int,
        objectMapper: ObjectMapper,
    ): Response =
        when (
            val insertedTasks =
                tryInsertBundleTasks(
                    tasksAndOrder = tasks,
                    bundleId = bundleId,
                    bundleOperations = bundleOperations
                )
        ) {
            is Failure -> objectMapper.sendBadRequestError(insertedTasks.reason.errorText)
            is Success -> sendBundleTasksResponse(
                bundleId = bundleId,
                objectMapper = objectMapper,
                insertedTasks = insertedTasks.value
            )
        }

    private fun tryUpdateTasks(
        tasks: List<TaskAndOrder>,
        bundleId: Int,
        objectMapper: ObjectMapper,
    ): Response =
        when (
            val insertedTasks =
                tryUpdateBundleTasks(
                    tasksAndOrder = tasks,
                    bundleId = bundleId,
                    bundleOperations = bundleOperations
                )
        ) {
            is Failure -> objectMapper.sendBadRequestError(insertedTasks.reason.errorText)
            is Success -> sendBundleTasksResponse(
                bundleId = bundleId,
                objectMapper = objectMapper,
                insertedTasks = insertedTasks.value
            )
        }

    private fun sendBundleTasksResponse(
        bundleId: Int,
        objectMapper: ObjectMapper,
        insertedTasks: List<TaskAndOrder>,
    ): Response =
        when (val bundle = selectBundle(bundleId, bundleOperations)) {
            is Failure -> objectMapper.sendBadRequestError(AddBundleTasksError.UNKNOWN_DATABASE_ERROR.errorText)
            is Success -> objectMapper.sendOKResponse(
                BundleTasksResponse(
                    bundle = bundle.value,
                    tasks = insertedTasks
                )
            )
        }
}
