package checkme.web.bundles.handlers

import checkme.domain.models.Bundle
import checkme.domain.models.User
import checkme.domain.operations.bundles.BundleOperationHolder
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.lenses.GeneralWebLenses.idOrNull
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.*
import org.http4k.lens.RequestContextLens

class ChangeBundleActualityHandler(
    private val bundleOperations: BundleOperationHolder,
    private val userLens: RequestContextLens<User?>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)
        val bundleId = request.idOrNull()
        return when {
            bundleId == null ->
                objectMapper.sendBadRequestError(ChangeBundleActualityError.NO_ID_FOR_BUNDLE.errorText)

            user == null || !user.isAdmin() ->
                objectMapper.sendBadRequestError(ChangeBundleActualityError.USER_HAS_NOT_RIGHTS.errorText)

            else -> {
                when (
                    val bundleToUpdateActuality = selectBundle(bundleId, bundleOperations)
                ) {
                    is Failure -> objectMapper.sendBadRequestError(ChangeBundleActualityError.NO_SUCH_BUNDLE.errorText)
                    is Success -> tryUpdateBundleActuality(
                        bundleToUpdateActuality = bundleToUpdateActuality.value,
                        bundleOperations = bundleOperations,
                        objectMapper = objectMapper
                    )
                }
            }
        }
    }
}

private fun tryUpdateBundleActuality(
    bundleToUpdateActuality: Bundle,
    bundleOperations: BundleOperationHolder,
    objectMapper: ObjectMapper,
): Response {
    val actuality: Boolean = !bundleToUpdateActuality.isActual
    return when (
        changeBundleActuality(
            bundleToUpdateActuality.copy(isActual = actuality),
            bundleOperations
        )
    ) {
        is Failure -> objectMapper.sendBadRequestError(
            ChangeBundleActualityError.UNKNOWN_DATABASE_ERROR.errorText
        )

        is Success -> objectMapper.sendOKResponse(null)
    }
}

enum class ChangeBundleActualityError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Something happened. Please try again later or ask for help"),
    NO_SUCH_BUNDLE("Bundle does not exist"),
    NO_ID_FOR_BUNDLE("No bundle id for change actuality"),
    USER_HAS_NOT_RIGHTS("Not allowed to change bundle actuality"),
}
