package checkme.domain.operations.dependencies.bundles

import checkme.domain.models.Bundle
import checkme.domain.models.TaskAndOrder
import dev.forkhandles.result4k.Result4k

interface BundlesDatabase {
    fun selectBundleById(bundleId: Int): Bundle?

    fun selectAllBundles(): List<Bundle>

    fun selectBundleTasksById(id: Int): List<TaskAndOrder>

    fun insertBundle(name: String): Bundle?

    fun insertBundleTasks(
        bundleId: Int,
        tasksAndOrder: List<TaskAndOrder>,
    ): List<TaskAndOrder>?

    fun selectHiddenBundles(): List<Bundle>

    fun updateBundleActuality(bundle: Bundle): Bundle?

    fun updateBundle(bundle: Bundle): Bundle?

    fun updateBundleTasks(
        bundleId: Int,
        newTasksAndOrder: List<TaskAndOrder>,
    ): List<TaskAndOrder>?

    fun deleteBundle(bundle: Bundle): Result4k<Boolean, BundleDatabaseError>
}

enum class BundleDatabaseError {
    UNKNOWN_DATABASE_ERROR,
}
