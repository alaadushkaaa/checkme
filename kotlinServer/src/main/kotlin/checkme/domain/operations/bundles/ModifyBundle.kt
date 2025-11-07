package checkme.domain.operations.bundles

import checkme.domain.models.Bundle
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import org.jooq.exception.DataAccessException

class ModifyBundle(
    private val updateBundle: (
        bundle: Bundle
    ) -> Bundle?,
) : (Bundle) -> Result4k<Bundle, ModifyBundleError> {
    override fun invoke(
        bundle: Bundle
    ): Result4k<Bundle, ModifyBundleError> =
        when (
            val editedBundle = updateBundle(bundle)
        ) {
            is Bundle -> Success(editedBundle)
            else -> Failure(ModifyBundleError.UNKNOWN_DATABASE_ERROR)
        }
}

class ModifyBundleActuality(
    private val updateBundleActuality: (
      bundle: Bundle
    ) -> Bundle?,
) : (Bundle) -> Result4k<Bundle, ModifyBundleError> {
    override fun invoke(
        bundle: Bundle
    ): Result4k<Bundle, ModifyBundleError> =
        when (
            val editedBundle = updateBundleActuality(
                bundle
            )
        ) {
            is Bundle -> Success(editedBundle)
            else -> Failure(ModifyBundleError.UNKNOWN_DATABASE_ERROR)
        }
}

class RemoveBundle(
    private val selectBundleById: (bundleId: Int) -> Bundle?,
    private val removeBundle: (Int) -> Int?,
) : (Bundle) -> Result<Int, BundleRemovingError> {
    override fun invoke(bundle: Bundle): Result<Int, BundleRemovingError> {
        return try {
            when {
                bundleNotExists(bundle.id) -> Failure(BundleRemovingError.BUNDLE_NOT_EXISTS)
                else -> when (removeBundle(bundle.id)) {
                    is Int -> Success(bundle.id)
                    else -> Failure(BundleRemovingError.UNKNOWN_DELETE_ERROR)
                }
            }
        } catch (_: DataAccessException) {
            Failure(BundleRemovingError.UNKNOWN_DATABASE_ERROR)
        }
    }

    private fun bundleNotExists(bundleId: Int): Boolean =
        when (selectBundleById(bundleId)) {
            is Bundle -> false
            else -> true
        }
}

enum class ModifyBundleError {
    UNKNOWN_DATABASE_ERROR,
}

enum class BundleRemovingError {
    UNKNOWN_DATABASE_ERROR,
    UNKNOWN_DELETE_ERROR,
    BUNDLE_NOT_EXISTS,
}