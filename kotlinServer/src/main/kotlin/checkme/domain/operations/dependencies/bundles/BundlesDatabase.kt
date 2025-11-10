package checkme.domain.operations.dependencies.bundles

import checkme.domain.models.Bundle
import checkme.domain.models.TaskAndPriority
import dev.forkhandles.result4k.Result4k

interface BundlesDatabase {
    fun selectBundleById(bundleId: Int): Bundle?

    fun selectAllBundles(): List<Bundle>

    fun selectBundleTasksById(id: Int): List<TaskAndPriority>

    fun insertBundle(name: String): Bundle?

    fun insertBundleTasks(
        bundleId: Int,
        tasksAndPriority: List<TaskAndPriority>,
    ): List<TaskAndPriority>?

    fun selectHiddenBundles(): List<Bundle>

    fun updateBundleActuality(bundle: Bundle): Bundle?

    fun updateBundle(bundle: Bundle): Bundle?

    fun updateBundleTasks(
        bundleId: Int,
        newTasksAndPriority: List<TaskAndPriority>,
    ): List<TaskAndPriority>?

    fun deleteBundle(bundleId: Int): Result4k<Boolean, BundleDatabaseError>
}

enum class BundleDatabaseError {
    UNKNOWN_DATABASE_ERROR,
}
