package checkme.web.bundles.handlers

import checkme.domain.models.User
import checkme.domain.operations.bundles.BundleOperationHolder
import checkme.logging.LoggerType
import checkme.logging.ServerLogger
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.lenses.BundleLenses
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.*
import org.http4k.lens.MultipartForm
import org.http4k.lens.RequestContextLens

class AddBundleHandler(
    private val bundleOperations: BundleOperationHolder,
    private val userLens: RequestContextLens<User?>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)
        val form: MultipartForm = BundleLenses.multipartFormFieldsAll(request)
        val bundleName = BundleLenses.nameField(form).value
        return when {
            user == null || !user.isAdmin() ->
                objectMapper.sendBadRequestError(AddBundleError.USER_HAS_NOT_RIGHTS.errorText)

            bundleName.isBlank() or bundleName.isEmpty() ->
                objectMapper.sendBadRequestError(AddBundleError.BUNDLE_NAME_CANNOT_BE_EMPTY_OR_BLANK.errorText)
            else -> {
                when (
                    val newBundle = addBundle(
                        bundleName = bundleName.trim(),
                        bundleOperations = bundleOperations
                    )
                ) {
                    is Success -> {
                        ServerLogger.log(
                            user = user,
                            action = "New bundle addition",
                            message = "Admin is created new bundle ${newBundle.value.id}-${newBundle.value.name}",
                            type = LoggerType.INFO
                        )
                        objectMapper.sendOKResponse(mapOf("bundleId" to newBundle.value.id))
                    }

                    is Failure -> {
                        ServerLogger.log(
                            user = user,
                            action = "New bundle addition warnings",
                            message = "Something wrong when try add bundle. Error: ${newBundle.reason.errorText}",
                            type = LoggerType.WARN
                        )
                        objectMapper.sendBadRequestError(newBundle.reason.errorText)
                    }
                }
            }
        }
    }
}
