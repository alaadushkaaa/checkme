package checkme.web.bundles.handlers

import checkme.domain.models.User
import checkme.domain.operations.bundles.BundleOperationHolder
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.lenses.GeneralWebLenses.idOrNull
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.lens.RequestContextLens

class BundleTasksHandler(
    private val userLens: RequestContextLens<User?>,
    private val bundleOperations: BundleOperationHolder,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)
        val bundleId = request.idOrNull()
        return when {
            user == null || !user.isAdmin() ->
                objectMapper.sendBadRequestError(ViewBundleTasksError.USER_CANT_VIEW_THIS_BUNDLE_TASKS.errorText)

            bundleId == null ->
                objectMapper.sendBadRequestError(ViewBundleTasksError.NO_BUNDLE_ID_ERROR.errorText)

            else -> {
                when (val bundleTasks = selectBundleTasks(bundleId, bundleOperations)) {
                    is Failure -> objectMapper.sendBadRequestError(bundleTasks.reason.errorText)

                    is Success -> objectMapper.sendOKResponse(bundleTasks.value)
                }
            }
        }
    }
}

enum class ViewBundleTasksError(val errorText: String) {
    NO_BUNDLE_ID_ERROR("No bundle id to view bundle tasks"),
    USER_CANT_VIEW_THIS_BUNDLE_TASKS("User can't view this bundle tasks"),
}
