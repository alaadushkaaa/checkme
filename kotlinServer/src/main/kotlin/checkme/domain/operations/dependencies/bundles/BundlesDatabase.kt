package checkme.domain.operations.dependencies.bundles

import checkme.domain.models.Bundle
import checkme.domain.models.TaskAndOrder
import dev.forkhandles.result4k.Result4k
import java.util.UUID

interface BundlesDatabase {
    fun selectBundleById(bundleId: UUID): Bundle?

    fun selectAllBundles(): List<Bundle>

    fun selectBundleTasksById(id: UUID): List<TaskAndOrder>

    fun insertBundle(name: String): Bundle?

    fun insertBundleTasks(
        bundleId: UUID,
        tasksAndOrder: List<TaskAndOrder>,
    ): List<TaskAndOrder>?

    fun selectHiddenBundles(): List<Bundle>

    fun updateBundleActuality(bundle: Bundle): Bundle?

    fun updateBundle(bundle: Bundle): Bundle?

    fun updateBundleTasks(
        bundleId: UUID,
        newTasksAndOrder: List<TaskAndOrder>,
    ): List<TaskAndOrder>?

    fun deleteBundle(bundle: Bundle): Result4k<Boolean, BundleDatabaseError>
}

enum class BundleDatabaseError {
    UNKNOWN_DATABASE_ERROR,
}
