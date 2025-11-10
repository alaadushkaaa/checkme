package checkme.domain.operations.bundles

import checkme.domain.models.Bundle
import checkme.domain.models.TaskAndPriority
import checkme.domain.operations.dependencies.bundles.BundlesDatabase
import dev.forkhandles.result4k.Result

class BundleOperationHolder(
    private val bundleDatabase: BundlesDatabase,
) {
    val fetchBundleById: (Int) -> Result<Bundle, BundleFetchingError> =
        FetchBundleById {
                bundleId: Int,
            ->
            bundleDatabase.selectBundleById(bundleId)
        }

    val fetchAllBundles: () -> Result<List<Bundle>, BundleFetchingError> =
        FetchAllBundles {
            bundleDatabase.selectAllBundles()
        }

    val fetchHiddenBundles: () -> Result<List<Bundle>, BundleFetchingError> =
        FetchHiddenBundles {
            bundleDatabase.selectHiddenBundles()
        }

    val fetchBundleTasksById: (bundleId: Int) -> Result<List<TaskAndPriority>, BundleFetchingError> =
        FetchBundleTasks (
            selectBundleById = bundleDatabase::selectBundleById,
            selectBundleTasks = bundleDatabase::selectBundleTasksById
        )

    val createBundle: (
        name: String,
    ) -> Result<Bundle, CreateBundleError> =
        CreateBundle {
                name: String,
            ->
            bundleDatabase.insertBundle(name)
        }

    val createBundleTasks: (
        bundleId: Int,
        tasks: List<TaskAndPriority>,
    ) -> Result<List<TaskAndPriority>, CreateBundleError> =
        CreateBundleTasks(
            bundleDatabase::selectBundleById,
            bundleDatabase::insertBundleTasks
        )

    val removeBundle: (bundle: Bundle) -> Result<Int, BundleRemovingError> =
        RemoveBundle(
            selectBundleById = bundleDatabase::selectBundleById,
            removeBundle = bundleDatabase::deleteBundle
        )

    val modifyBundleActuality: (bundle: Bundle) -> Result<Bundle, ModifyBundleError> =
        ModifyBundleActuality {
                bundle: Bundle,
            ->
            bundleDatabase.updateBundleActuality(bundle)
        }

    val modifyBundle: (bundle: Bundle) -> Result<Bundle, ModifyBundleError> =
        ModifyBundle {
                bundle: Bundle,
            ->
            bundleDatabase.updateBundle(bundle)
        }

    val modifyBundleTasks: (
        bundleId: Int,
        newTasksAndPriority: List<TaskAndPriority>,
    ) -> Result<List<TaskAndPriority>, ModifyBundleError> =
        ModifyBundleTasks (
            bundleDatabase::selectBundleById,
            bundleDatabase::updateBundleTasks
        )
}
