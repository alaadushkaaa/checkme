package checkme.web.bundles.handlers

import checkme.domain.models.Bundle
import checkme.domain.models.User
import checkme.domain.operations.bundles.BundleOperationHolder
import checkme.logging.LoggerType
import checkme.logging.ServerLogger
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.lenses.GeneralWebLenses.idOrNull
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.*
import org.http4k.lens.RequestContextLens

class DeleteBundleHandler(
    private val bundleOperations: BundleOperationHolder,
    private val userLens: RequestContextLens<User?>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)
        val bundleId = request.idOrNull()
            ?: return objectMapper.sendBadRequestError(DeleteBundleError.NO_ID_TO_DELETE_BUNDLE.errorText)
        return when {
            user?.isAdmin() == true ->
                when (
                    val bundleToDelete = selectBundle(
                        bundleId = bundleId,
                        bundleOperations = bundleOperations
                    )
                ) {
                    is Failure -> objectMapper.sendBadRequestError(DeleteBundleError.BUNDLE_NOT_EXISTS)
                    is Success -> tryDeleteBundle(
                        user = user,
                        bundleToDelete = bundleToDelete.value,
                        objectMapper = objectMapper,
                        bundleOperations = bundleOperations
                    )
                }

            else -> objectMapper.sendBadRequestError(DeleteBundleError.USER_HAS_NOT_RIGHTS.errorText)
        }
    }
}

private fun tryDeleteBundle(
    user: User,
    bundleToDelete: Bundle,
    objectMapper: ObjectMapper,
    bundleOperations: BundleOperationHolder,
): Response {
    return when (
        val deleteFlag = deleteBundle(
            bundle = bundleToDelete,
            bundleOperations = bundleOperations
        )
    ) {
        is Failure -> objectMapper.sendBadRequestError(deleteFlag.reason.errorText)

        is Success -> {
            ServerLogger.log(
                user = user,
                action = "Bundle deletion",
                message = "User delete bundle ${bundleToDelete.id}-${bundleToDelete.name}",
                type = LoggerType.INFO
            )
            objectMapper.sendOKResponse(mapOf("status" to "complete"))
        }
    }
}

enum class DeleteBundleError(val errorText: String) {
    NO_ID_TO_DELETE_BUNDLE("No id to delete bundle"),
    BUNDLE_NOT_EXISTS("Bundle with this id doesnt exists"),
    USER_HAS_NOT_RIGHTS("Not allowed to delete bundle"),
}