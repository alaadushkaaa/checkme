package checkme.domain.operations.dependencies.bundles

import checkme.domain.models.Bundle

interface BundlesDatabase {
    fun selectBundleById(bundleId: Int): Bundle?

    fun selectAllBundles(): List<Bundle>

    fun insertBundle(
        name: String,
        tasks: Map<Int, Int>,
    ): Bundle?

    fun selectHiddenBundles(): List<Bundle>

    fun updateBundleActuality(bundle: Bundle): Bundle?

    fun updateBundle(bundle: Bundle): Bundle?

    fun deleteBundle(bundleId: Int): Int
}
