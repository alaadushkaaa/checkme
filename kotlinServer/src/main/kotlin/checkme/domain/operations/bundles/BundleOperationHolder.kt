package checkme.domain.operations.bundles

import checkme.domain.models.Bundle
import checkme.domain.models.TaskAndOrder
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

    val fetchBundleTasksById: (bundleId: Int) -> Result<List<TaskAndOrder>, BundleFetchingError> =
        FetchBundleTasks {
                bundleId,
            ->
            bundleDatabase.selectBundleTasksById(bundleId)
        }

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
        tasks: List<TaskAndOrder>,
    ) -> Result<List<TaskAndOrder>, CreateBundleTasksError> =
        CreateBundleTasks(
            bundleDatabase::selectBundleById,
            bundleDatabase::insertBundleTasks
        )

    val removeBundle: (bundle: Bundle) -> Result<Boolean, BundleRemovingError> =
        RemoveBundle (
            bundleDatabase::selectBundleById,
            bundleDatabase::deleteBundle
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
        newTasksAndOrder: List<TaskAndOrder>,
    ) -> Result<List<TaskAndOrder>, ModifyBundleError> =
        ModifyBundleTasks(
            bundleDatabase::selectBundleById,
            bundleDatabase::updateBundleTasks
        )
}
