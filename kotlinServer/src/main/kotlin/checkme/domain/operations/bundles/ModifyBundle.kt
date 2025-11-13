package checkme.domain.operations.bundles

import checkme.domain.models.Bundle
import checkme.domain.models.TaskAndOrder
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
        newTasksAndOrder: List<TaskAndOrder>,
    ) -> List<TaskAndOrder>?,
) : (Int, List<TaskAndOrder>) -> Result4k<List<TaskAndOrder>, ModifyBundleError> {
    override fun invoke(
        bundleId: Int,
        newTasksAndOrder: List<TaskAndOrder>,
    ): Result4k<List<TaskAndOrder>, ModifyBundleError> =
        try {
            when {
                bundleNotExists(bundleId) -> Failure(ModifyBundleError.NO_SUCH_BUNDLE)
                else -> when (
                    val updatedTasks = updateBundleTasks(bundleId, newTasksAndOrder)
                ) {
                    is List<TaskAndOrder> -> Success(updatedTasks)
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
    private val removeBundle: (Bundle) -> Result4k<Boolean, BundleDatabaseError>,
) : (Bundle) -> Result<Boolean, BundleRemovingError> {
    override fun invoke(bundle: Bundle): Result<Boolean, BundleRemovingError> {
        return when (removeBundle(bundle)) {
            is Success -> Success(true)
            else -> Failure(BundleRemovingError.UNKNOWN_DELETE_ERROR)
        }
    }
}

enum class ModifyBundleError {
    UNKNOWN_DATABASE_ERROR,
    NO_SUCH_BUNDLE,
}

enum class BundleRemovingError {
    UNKNOWN_DATABASE_ERROR,
    UNKNOWN_DELETE_ERROR,
}
