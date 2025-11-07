package checkme.domain.operations.bundles

import checkme.domain.models.Bundle
import checkme.domain.operations.dependencies.bundles.BundlesDatabase
import dev.forkhandles.result4k.Result

class BundleOperationHolder(
    private val bundleDatabase: BundlesDatabase,
){
    val fetchBundleById: (Int) -> Result<Bundle, BundleFetchingError> =
        FetchBundleById {
            bundleId: Int
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

    val createBundle: (
            name: String,
            tasks: Map<Int, Int>
    ) -> Result<Bundle, CreateBundleError> =
        CreateBundle {
                name: String,
                tasks: Map<Int, Int>,
             ->
            bundleDatabase.insertBundle(
                name,
                tasks
            )
        }

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
}