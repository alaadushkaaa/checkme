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
}