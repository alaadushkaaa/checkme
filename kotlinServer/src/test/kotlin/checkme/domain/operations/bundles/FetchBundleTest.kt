package checkme.domain.operations.bundles

import checkme.db.validBundleTasksAsATable
import checkme.db.validBundles
import checkme.domain.models.Bundle
import checkme.domain.models.TaskAndOrder
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class FetchBundleTest : FunSpec({
    val tasks = validBundleTasksAsATable
    val bundles = validBundles
    val bundle = validBundles.first()

    val fetchAllBundlesMock: () -> List<Bundle> = { bundles.filter { it.isActual } }
    val fetchHiddenBundlesMock: () -> List<Bundle> = { bundles.filter { !it.isActual } }
    val fetchOneBundleMock: () -> List<Bundle> = { listOf(bundle) }
    val fetchBundleByIdMock: (Int) -> Bundle? = { id -> bundles.firstOrNull { it.id == id } }
    val fetchBundleTasksByIdMock: (Int) -> List<TaskAndOrder> =
        { id -> validBundleTasksAsATable.filter { it.key == id }.values.toList() }

    val fetchBundleTasksById = FetchBundleTasks(fetchBundleTasksByIdMock)
    val fetchHiddenBundles = FetchHiddenBundles(fetchHiddenBundlesMock)
    val fetchOneBundleAsList = FetchAllBundles(fetchOneBundleMock)
    val fetchAllBundles = FetchAllBundles(fetchAllBundlesMock)
    val fetchBundleById = FetchBundleById(fetchBundleByIdMock)

    test("Fetch all bundles should return list of bundles and list have size more than one") {
        fetchAllBundles().shouldBeSuccess().shouldHaveSize(bundles.filter { it.isActual }.size)
    }

    test("Fetch hidden bundles should return tasks with isActual = false") {
        fetchHiddenBundles().shouldBeSuccess().shouldHaveSize(bundles.filter { !it.isActual }.size)
    }

    test("Fetch all bundles should return list with one bundle when only one bundle existed") {
        fetchOneBundleAsList().shouldBeSuccess().shouldHaveSize(1)
    }

    test("Fetch bundle by valid id should return this bundle") {
        fetchBundleById(bundles.first().id).shouldBeSuccess().shouldBe(bundles.first())
    }

    test("Fetch bundle by invalid id should return an error") {
        fetchBundleById(bundles.size + 1).shouldBeFailure(BundleFetchingError.NO_SUCH_BUNDLE)
    }

    test("Fetch bundle tasks by id should return bundle tasks") {
        fetchBundleTasksById(bundles.first().id).shouldBeSuccess()
            .shouldContainExactlyInAnyOrder(tasks.filter { it.key == bundles.first().id }.values.toList())
    }

    test("Fetch bundle tasks should return empty list if bundle has no tasks") {
        fetchBundleTasksById(bundles[2].id).shouldBeSuccess().shouldBeEmpty()
    }

    test("Fetch bundle tasks by invalid bundle id should return an empty list") {
        fetchBundleTasksById(bundles.size + 1).shouldBeSuccess().shouldBeEmpty()
    }
})
