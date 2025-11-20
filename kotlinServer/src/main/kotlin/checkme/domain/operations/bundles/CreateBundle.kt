package checkme.domain.operations.bundles

import checkme.domain.models.Bundle
import checkme.domain.models.TaskAndOrder
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
        tasks: List<TaskAndOrder>,
    ) -> List<TaskAndOrder>?,
) : (
        Int,
        List<TaskAndOrder>,
    ) -> Result4k<List<TaskAndOrder>, CreateBundleTasksError> {
    override fun invoke(
        bundleId: Int,
        tasks: List<TaskAndOrder>,
    ): Result4k<List<TaskAndOrder>, CreateBundleTasksError> =
        try {
            when {
                bundleNotExists(bundleId) -> Failure(CreateBundleTasksError.NO_SUCH_BUNDLE_FOR_TASKS)
                tasks.isEmpty() -> Failure(CreateBundleTasksError.TASKS_LIST_IS_EMPTY)
                else ->
                    when (
                        val insertedTasks = insertBundleTasks(
                            bundleId,
                            tasks
                        )
                    ) {
                        is List<TaskAndOrder> -> Success(insertedTasks)
                        else -> {
                            Failure(CreateBundleTasksError.UNKNOWN_DATABASE_ERROR)
                        }
                    }
            }
        } catch (_: DataAccessException) {
            Failure(CreateBundleTasksError.UNKNOWN_DATABASE_ERROR)
        }

    private fun bundleNotExists(bundleId: Int): Boolean =
        when (selectBundleById(bundleId)) {
            is Bundle -> false
            else -> true
        }
}

enum class CreateBundleError {
    UNKNOWN_DATABASE_ERROR,
}

enum class CreateBundleTasksError {
    NO_SUCH_BUNDLE_FOR_TASKS,
    TASKS_LIST_IS_EMPTY,
    UNKNOWN_DATABASE_ERROR,
}
