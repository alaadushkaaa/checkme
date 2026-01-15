package checkme.web.bundles.handlers

import checkme.domain.models.Bundle
import checkme.domain.models.User
import checkme.domain.operations.bundles.BundleOperationHolder
import checkme.logging.LoggerType
import checkme.logging.ServerLogger
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.lenses.BundleLenses
import checkme.web.lenses.GeneralWebLenses.idOrNull
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.lens.MultipartForm
import org.http4k.lens.RequestContextLens

class ChangeBundleNameHandler(
    private val bundleOperations: BundleOperationHolder,
    private val userLens: RequestContextLens<User?>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)
        val bundleId = request.idOrNull()
        val form: MultipartForm = BundleLenses.multipartFormFieldsAll(request)
        return when {
            user == null || !user.isAdmin() ->
                objectMapper.sendBadRequestError(AddBundleError.USER_HAS_NOT_RIGHTS.errorText)

            bundleId == null ->
                objectMapper.sendBadRequestError(BundleChangingError.NO_BUNDLE_ID_FOR_CHANGE.errorText)

            else -> {
                val bundleName = BundleLenses.nameField(form).value
                when (
                    val bundleToUpdateName = selectBundle(bundleId, bundleOperations)
                ) {
                    is Failure -> {
                        ServerLogger.log(
                            user = user,
                            action = "Select bundle error",
                            message = "Error: ${BundleChangingError.NO_SUCH_BUNDLE.errorText} while " +
                                "try to select bundle with id = $bundleId",
                            type = LoggerType.INFO
                        )
                        objectMapper.sendBadRequestError(BundleChangingError.NO_SUCH_BUNDLE)
                    }

                    is Success -> tryUpdateBundleName(
                        user = user,
                        bundleToUpdateName = bundleToUpdateName.value.copy(name = bundleName),
                        bundleOperations = bundleOperations,
                        objectMapper = objectMapper
                    )
                }
            }
        }
    }
}

private fun tryUpdateBundleName(
    user: User,
    bundleToUpdateName: Bundle,
    bundleOperations: BundleOperationHolder,
    objectMapper: ObjectMapper,
): Response {
    return when (
        changeBundleName(
            bundleToUpdateName,
            bundleOperations
        )
    ) {
        is Failure -> {
            ServerLogger.log(
                user = user,
                action = "Bundle correction error",
                message = "Error while try to change bundle name for bundle ${bundleToUpdateName.id} - " +
                    "${bundleToUpdateName.name}. " +
                    "Error: ${BundleChangingError.UNKNOWN_DATABASE_ERROR.errorText}",
                type = LoggerType.INFO
            )
            objectMapper.sendBadRequestError(
                ChangeBundleActualityError.UNKNOWN_DATABASE_ERROR.errorText
            )
        }

        is Success -> {
            ServerLogger.log(
                user = user,
                action = "Bundle correction",
                message = "Admin change bundle name for ${bundleToUpdateName.id}-" +
                    "${bundleToUpdateName.name}. Now bundle name is ${bundleToUpdateName.name}",
                type = LoggerType.INFO
            )
            objectMapper.sendOKResponse(mapOf("bundleId" to bundleToUpdateName.id))
        }
    }
}
