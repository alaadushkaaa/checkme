package checkme.web.bundles.handlers

import checkme.domain.models.User
import checkme.domain.operations.bundles.BundleOperationHolder
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.*
import org.http4k.lens.RequestContextLens

class BundleHiddenListHandler(
    private val bundleOperations: BundleOperationHolder,
    private val userLens: RequestContextLens<User?>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)
        return when {
            user == null -> objectMapper.sendBadRequestError(ViewBundleListError.USER_CANT_VIEW_BUNDLES.errorText)
            else -> tryFetchBundles(
                bundleOperations = bundleOperations,
                objectMapper = objectMapper
            )
        }
    }
}

private fun tryFetchBundles(
    bundleOperations: BundleOperationHolder,
    objectMapper: ObjectMapper,
): Response {
    return when (
        val bundles = selectHiddenBundles(bundleOperations)
    ) {
        is Failure -> objectMapper.sendBadRequestError(bundles.reason.errorText)
        is Success -> objectMapper.sendOKResponse(bundles.value)
    }
}
