package checkme.db.bundles

import checkme.db.TestcontainerSpec
import checkme.db.validBundles
import checkme.domain.models.Bundle
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class UpdateBundleTest : TestcontainerSpec({ context ->
    val bundleOperations = BundleOperations(context)

    lateinit var insertedBundles: List<Bundle>

    beforeEach {
        insertedBundles =
            validBundles.map {
                bundleOperations.insertBundle(
                    it.name,
                    it.tasks
                ).shouldNotBeNull()
            }
    }

    test("Valid bundle can be removed") {
        val bundleForRemove = validBundles.first()
        bundleOperations.deleteBundle(bundleForRemove.id).shouldBe(1)
    }

    test("Only one bundle can be deleted") {
        val bundleForRemove = validBundles.first()
        bundleOperations.deleteBundle(bundleForRemove.id).shouldBe(1)
        bundleOperations.selectAllBundles().shouldBe(validBundles.subList(2, validBundles.size))
        bundleOperations.deleteBundle(validBundles[1].id).shouldBe(1)
        bundleOperations.selectAllBundles().shouldBe(validBundles.subList(2, validBundles.size))
    }

    test("Cant delete bundle by invalid id") {
        bundleOperations.deleteBundle(validBundles.size + 1).shouldBe(0)
    }

    test("Bundle can be updated") {
        val newBundle = validBundles.first().copy(id = validBundles[1].id)
        bundleOperations.updateBundle(newBundle).shouldNotBeNull()
        val updatedBundle = bundleOperations.selectBundleById(validBundles[1].id).shouldNotBeNull()

        updatedBundle.id shouldBe validBundles[1].id
        updatedBundle.name shouldBe validBundles.first().name
        updatedBundle.tasks shouldBe validBundles.first().tasks
        updatedBundle.isActual shouldBe validBundles.first().isActual
    }

    test("Bundle actuality can be updated") {
        val bundleWithUpdatedActuality =
            bundleOperations.updateBundleActuality(validBundles.first().copy(isActual = false)).shouldNotBeNull()

        bundleWithUpdatedActuality.id shouldBe validBundles[1].id
        bundleWithUpdatedActuality.name shouldBe validBundles.first().name
        bundleWithUpdatedActuality.tasks shouldBe validBundles.first().tasks
        bundleWithUpdatedActuality.isActual shouldBe !validBundles.first().isActual
    }
})
