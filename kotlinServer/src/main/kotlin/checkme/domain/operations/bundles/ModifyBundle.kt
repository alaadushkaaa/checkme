package checkme.domain.operations.bundles

import checkme.domain.models.Bundle
import checkme.domain.models.TaskAndPriority
import checkme.domain.operations.dependencies.bundles.BundleDatabaseError
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import org.jooq.exception.DataAccessException

class ModifyBundle(
    private val updateBundle: (
        bundle: Bundle,
    ) -> Bundle?,
) : (Bundle) -> Result4k<Bundle, ModifyBundleError> {
    override fun invoke(bundle: Bundle): Result4k<Bundle, ModifyBundleError> =
        when (
            val editedBundle = updateBundle(bundle)
        ) {
            is Bundle -> Success(editedBundle)
            else -> Failure(ModifyBundleError.UNKNOWN_DATABASE_ERROR)
        }
}

class ModifyBundleActuality(
    private val updateBundleActuality: (
        bundle: Bundle,
    ) -> Bundle?,
) : (Bundle) -> Result4k<Bundle, ModifyBundleError> {
    override fun invoke(bundle: Bundle): Result4k<Bundle, ModifyBundleError> =
        when (
            val editedBundle = updateBundleActuality(
                bundle
            )
        ) {
            is Bundle -> Success(editedBundle)
            else -> Failure(ModifyBundleError.UNKNOWN_DATABASE_ERROR)
        }
}

class ModifyBundleTasks(
    private val selectBundleById: (bundleId: Int) -> Bundle?,
    private val updateBundleTasks: (
        bundleId: Int,
        newTasksAndPriority: List<TaskAndPriority>,
    ) -> List<TaskAndPriority>?,
) : (Int, List<TaskAndPriority>) -> Result4k<List<TaskAndPriority>, ModifyBundleError> {
    override fun invoke(
        bundleId: Int,
        newTasksAndPriority: List<TaskAndPriority>,
    ): Result4k<List<TaskAndPriority>, ModifyBundleError> =
        try {
            when {
                bundleNotExists(bundleId) -> Failure(ModifyBundleError.NO_SUCH_BUNDLE)
                else -> when (
                    val updatedTasks = updateBundleTasks(bundleId, newTasksAndPriority)
                ) {
                    is List<TaskAndPriority> -> Success(updatedTasks)
                    else -> Failure(ModifyBundleError.UNKNOWN_DATABASE_ERROR)
                }
            }
        } catch (_: DataAccessException) {
            Failure(ModifyBundleError.UNKNOWN_DATABASE_ERROR)
        }

    private fun bundleNotExists(bundleId: Int): Boolean =
        when (selectBundleById(bundleId)) {
            is Bundle -> false
            else -> true
        }
}

class RemoveBundle(
    private val selectBundleById: (bundleId: Int) -> Bundle?,
    private val removeBundle: (Int) -> Result4k<Boolean, BundleDatabaseError>,
) : (Bundle) -> Result<Int, BundleRemovingError> {
    override fun invoke(bundle: Bundle): Result<Int, BundleRemovingError> {
        return try {
            when {
                bundleNotExists(bundle.id) -> Failure(BundleRemovingError.BUNDLE_NOT_EXISTS)
                else -> when (removeBundle(bundle.id)) {
                    is Success -> Success(bundle.id)
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
    NO_SUCH_BUNDLE,
}

enum class BundleRemovingError {
    UNKNOWN_DATABASE_ERROR,
    UNKNOWN_DELETE_ERROR,
    BUNDLE_NOT_EXISTS,
}
