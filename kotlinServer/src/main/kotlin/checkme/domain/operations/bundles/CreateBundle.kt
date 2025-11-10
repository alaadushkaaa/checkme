package checkme.domain.operations.bundles

import checkme.domain.models.Bundle
import checkme.domain.models.TaskAndPriority
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import org.jooq.exception.DataAccessException

class CreateBundle(
    private val insertBundle: (
        name: String,
    ) -> Bundle?,
) : (
        String,
    ) -> Result4k<Bundle, CreateBundleError> {
    override fun invoke(name: String): Result4k<Bundle, CreateBundleError> =
        when (
            val newBundle = insertBundle(
                name
            )
        ) {
            is Bundle -> Success(newBundle)
            else -> {
                Failure(CreateBundleError.UNKNOWN_DATABASE_ERROR)
            }
        }
}

class CreateBundleTasks(
    private val selectBundleById: (bundleId: Int) -> Bundle?,
    private val insertBundleTasks: (
        bundleId: Int,
        tasks: List<TaskAndPriority>,
    ) -> List<TaskAndPriority>?,
) : (
        Int,
        List<TaskAndPriority>,
    ) -> Result4k<List<TaskAndPriority>, CreateBundleError> {
    override fun invoke(
        bundleId: Int,
        tasks: List<TaskAndPriority>,
    ): Result4k<List<TaskAndPriority>, CreateBundleError> =
        try {
            when {
                bundleNotExists(bundleId) -> Failure(CreateBundleError.NO_SUCH_BUNDLE_FOR_TASKS)
                else -> when (
                    val insertedTasks = insertBundleTasks(
                        bundleId,
                        tasks
                    )
                ) {
                    is List<TaskAndPriority> -> Success(insertedTasks)
                    else -> {
                        Failure(CreateBundleError.UNKNOWN_DATABASE_ERROR)
                    }
                }
            }
        } catch (_: DataAccessException) {
            Failure(CreateBundleError.UNKNOWN_DATABASE_ERROR)
        }

    private fun bundleNotExists(bundleId: Int): Boolean =
        when (selectBundleById(bundleId)) {
            is Bundle -> false
            else -> true
        }
}

enum class CreateBundleError {
    UNKNOWN_DATABASE_ERROR,
    NO_SUCH_BUNDLE_FOR_TASKS,
}
