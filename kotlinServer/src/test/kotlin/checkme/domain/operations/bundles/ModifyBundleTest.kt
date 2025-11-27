package checkme.domain.operations.bundles

import checkme.db.validBundleTasks
import checkme.db.validBundles
import checkme.domain.models.Bundle
import checkme.domain.models.TaskAndOrder
import checkme.domain.operations.dependencies.bundles.BundleDatabaseError
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class ModifyBundleTest : FunSpec({
    isolationMode = IsolationMode.InstancePerTest

    val validBundlesList = validBundles.toMutableList()
    val validBundleTasksInserted = mutableMapOf(
        validBundlesList[0].id to validBundleTasks,
        validBundlesList[1].id to validBundleTasks.subList(0, 1)
    )

    val selectBundleByIdMock: (Int) -> Bundle? = { id -> validBundlesList.find { it.id == id } }
    val selectBundleByIdNullMock: (Int) -> Bundle? = { null }
    val deleteBundleMock: (Bundle) -> Result4k<Boolean, BundleDatabaseError> =
        { bundle ->
            when (validBundlesList.find { it.id == bundle.id }) {
                is Bundle -> Success(true)
                else -> Failure(BundleDatabaseError.UNKNOWN_DATABASE_ERROR)
            }
        }

    val updateBundleMock: (Bundle) -> Bundle? = { bundle ->
        val bundleToReplace = validBundles.firstOrNull { it.id == bundle.id }
        if (bundleToReplace != null) {
            validBundlesList.add(
                bundleToReplace.copy(
                    name = bundle.name,
                    isActual = bundle.isActual
                )
            )
            validBundlesList.remove(bundleToReplace)
        }
        validBundlesList.firstOrNull { it.id == bundle.id }
    }

    val updateBundleActualityMock: (Bundle) -> Bundle? =
        { bundle -> validBundlesList.find { it.id == bundle.id }?.copy(isActual = !bundle.isActual) }

    val updateBundleTasksMock: (Int, List<TaskAndOrder>) -> List<TaskAndOrder>? =
        { bundleId, tasks ->
            if (validBundles.firstOrNull { it.id == bundleId } != null) {
                validBundleTasksInserted[bundleId] = tasks
                validBundleTasksInserted[bundleId]
            } else {
                null
            }
        }

    val deleteBundle = RemoveBundle(selectBundleByIdMock, deleteBundleMock)
    val deleteBundleNotExists = RemoveBundle(selectBundleByIdNullMock, deleteBundleMock)
    val updateBundle = ModifyBundle(updateBundleMock)
    val updateBundleActuality = ModifyBundleActuality(updateBundleActualityMock)
    val updateBundleTasks = ModifyBundleTasks(selectBundleByIdMock, updateBundleTasksMock)
    val updateBundleTasksBundleNotExist = ModifyBundleTasks(selectBundleByIdNullMock, updateBundleTasksMock)

    test("Bundle can be deleted if bundle exists") {
        deleteBundle(validBundlesList.first()).shouldBeSuccess()
    }

    test("Bundle cant be deleted if bundle doesn't exists") {
        deleteBundleNotExists(
            validBundlesList.first()
                .copy(id = validBundlesList.maxOf { it.id } + 1)
        ).shouldBeFailure(BundleRemovingError.BUNDLE_NOT_EXISTS)
        validBundlesList.find { it.id == validBundlesList.maxOf { it.id } + 1 }.shouldBeNull()
    }

    test("Can update valid bundle") {
        val bundleForUpdate = validBundlesList.first().copy(name = "New name")
        val updatedBundle = updateBundle(bundleForUpdate).shouldBeSuccess()
        updatedBundle.id.shouldBe(bundleForUpdate.id)
        updatedBundle.name.shouldBe(bundleForUpdate.name)
        updatedBundle.isActual.shouldBe(bundleForUpdate.isActual)
    }

    test("Bundle cant be updated when bundle not exists") {
        updateBundle(
            validBundlesList.first().copy(id = validBundlesList.maxOf { it.id } + 1)
        )
            .shouldBeFailure(ModifyBundleError.UNKNOWN_DATABASE_ERROR)
    }

    test("Bundle actuality can be updated") {
        val actuality = validBundlesList.first().isActual
        val updatedBundle = updateBundleActuality(validBundlesList.first().copy(isActual = actuality)).shouldBeSuccess()
        updatedBundle.isActual.shouldBe(!actuality)
    }

    test("Cant update bundle actuality if bundle not exists") {
        updateBundleActuality(
            validBundlesList.first().copy(id = validBundlesList.maxOf { it.id } + 1)
        )
            .shouldBeFailure(ModifyBundleError.UNKNOWN_DATABASE_ERROR)
    }

    test("Can update bundle tasks") {
        val updatedTasks =
            updateBundleTasks(validBundlesList.first().id, validBundleTasks.subList(0, 1)).shouldBeSuccess()
        updatedTasks.shouldContainExactlyInAnyOrder(validBundleTasks.subList(0, 1))
        validBundleTasksInserted[validBundlesList.first().id].shouldContainExactlyInAnyOrder(updatedTasks)
    }

    test("Cant update bundle tasks if bundle not exists") {
        updateBundleTasksBundleNotExist(
            validBundlesList.maxOf { it.id } + 1,
            validBundleTasks.subList(0, 1)
        ).shouldBeFailure(ModifyBundleError.NO_SUCH_BUNDLE)
    }
})
