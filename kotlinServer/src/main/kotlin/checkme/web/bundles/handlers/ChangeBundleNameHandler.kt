package checkme.web.bundles.handlers

import checkme.domain.models.Bundle
import checkme.domain.models.User
import checkme.domain.operations.bundles.BundleOperationHolder
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.lenses.BundleLenses
import checkme.web.lenses.GeneralWebLenses.idOrNull
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.*
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
            bundleId == null ->
                objectMapper.sendBadRequestError(ChangeBundleNameError.NO_ID_FOR_BUNDLE.errorText)

            user == null || !user.isAdmin() ->
                objectMapper.sendBadRequestError(ChangeBundleNameError.USER_HAS_NOT_RIGHTS.errorText)

            else -> {
                val bundleNewName = BundleLenses.nameField(form).value
                when (
                    val fetchedBundle = selectBundle(bundleId, bundleOperations)
                ) {
                    is Failure -> objectMapper.sendBadRequestError(ChangeBundleNameError.NO_SUCH_BUNDLE.errorText)
                    is Success -> {
                        val bundleToUpdate = fetchedBundle.value.copy(name = bundleNewName)
                        if (!bundleAlreadyHaveThisName(fetchedBundle.value.name, bundleNewName)) {
                            tryUpdateBundleName(
                                bundleToUpdateName = bundleToUpdate,
                                bundleOperations = bundleOperations,
                                objectMapper = objectMapper
                            )
                        } else {
                            objectMapper.sendBadRequestError(ChangeBundleNameError.NAME_ALREADY_EXISTS.errorText)
                        }
                    }
                }
            }
        }
    }
}

private fun bundleAlreadyHaveThisName(
    oldName: String,
    newName: String,
): Boolean = oldName == newName

private fun tryUpdateBundleName(
    bundleToUpdateName: Bundle,
    bundleOperations: BundleOperationHolder,
    objectMapper: ObjectMapper,
): Response {
    return when (
        changeBundleActuality(bundleToUpdateName, bundleOperations)
    ) {
        is Failure -> objectMapper.sendBadRequestError(
            ChangeBundleNameError.UNKNOWN_DATABASE_ERROR.errorText
        )

        is Success -> objectMapper.sendOKResponse(null)
    }
}

enum class ChangeBundleNameError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Something happened. Please try again later or ask for help"),
    NO_SUCH_BUNDLE("Bundle does not exist"),
    NO_ID_FOR_BUNDLE("No bundle id for change name"),
    NAME_ALREADY_EXISTS("Bundle already have this name"),
    USER_HAS_NOT_RIGHTS("Not allowed to change bundle name"),
}
