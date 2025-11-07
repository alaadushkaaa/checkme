package checkme.db.bundles

import checkme.db.TestcontainerSpec
import checkme.db.validBundles
import checkme.db.validChecks
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class InsertBundleTest: TestcontainerSpec({ context ->
    val bundleOperations = BundlesOperations(context)

    test("Valid bundle insertions should return this bundle ") {
        val bundleForInsert = validBundles.first()
        val insertedBundle =
            bundleOperations.insertBundle(
                bundleForInsert.name,
                bundleForInsert.tasks
            ).shouldNotBeNull()

        insertedBundle.tasks.shouldBe(bundleForInsert.name)
        insertedBundle.name.shouldBe(bundleForInsert.tasks)
        insertedBundle.isActual.shouldBe(true)
    }
})