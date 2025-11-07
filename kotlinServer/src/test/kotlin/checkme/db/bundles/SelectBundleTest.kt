package checkme.db.bundles

import checkme.db.TestcontainerSpec
import checkme.db.checks.CheckOperations
import checkme.db.validBundles
import checkme.db.validChecks
import checkme.db.validChecksMany
import checkme.domain.models.Bundle
import checkme.domain.models.Check
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class SelectBundleTest : TestcontainerSpec({ context ->
    val bundleOperations = BundlesOperations(context)

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

    test("Select bundle by id should return this bundle") {
        val selectedCheck = bundleOperations.selectBundleById(insertedBundles.first().id).shouldNotBeNull()

        selectedCheck.id.shouldBe(insertedBundles.first().id)
        selectedCheck.name.shouldBe(insertedBundles.first().name)
        selectedCheck.tasks.shouldBe(insertedBundles.first().tasks)
        selectedCheck.isActual.shouldBe(true)
    }

    test("Select bundle by invalid id should return null") {
        bundleOperations.selectBundleById(insertedBundles.maxOf { it.id } + 1).shouldBeNull()
    }

    test("Select all bundles should return all of this inserted bundles") {
        val selectedBundles = bundleOperations.selectAllBundles().shouldNotBeNull()
        selectedBundles shouldContainExactlyInAnyOrder insertedBundles
    }
})