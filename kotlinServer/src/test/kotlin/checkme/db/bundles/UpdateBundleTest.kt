package checkme.db.bundles

import checkme.db.TestcontainerSpec
import checkme.db.tasks.TasksOperations
import checkme.db.validBundleTasks
import checkme.db.validBundles
import checkme.db.validTasks
import checkme.domain.models.Bundle
import checkme.domain.models.TaskAndOrder
import dev.forkhandles.result4k.Success
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class UpdateBundleTest : TestcontainerSpec({ context ->
    val tasksOperations = TasksOperations(context)
    val bundleOperations = BundleOperations(context, tasksOperations)

    lateinit var insertedBundles: List<Bundle>
    lateinit var insertedBundleTasks: List<TaskAndOrder>

    beforeEach {
        for (task in validTasks) {
            tasksOperations.insertTask(
                task.name,
                task.criterions,
                task.answerFormat,
                task.description,
                task.isActual
            ).shouldNotBeNull()
        }
        insertedBundles =
            validBundles.map {
                bundleOperations.insertBundle(
                    it.name,
                ).shouldNotBeNull()
            }
        insertedBundleTasks =
            bundleOperations.insertBundleTasks(insertedBundles.first().id, validBundleTasks).shouldNotBeNull()
    }

    test("Valid bundle can be removed") {
        val bundleForRemove = validBundles.first()
        bundleOperations.deleteBundle(bundleForRemove).shouldBe(Success(true))
    }

    test("Only one bundle can be deleted") {
        val bundleForRemove = validBundles.first()
        bundleOperations.deleteBundle(bundleForRemove).shouldBe(Success(true))
        bundleOperations.selectAllBundles().shouldBe(validBundles.subList(1, validBundles.size))
        bundleOperations.deleteBundle(validBundles[1]).shouldBe(Success(true))
        bundleOperations.selectAllBundles().shouldBe(validBundles.subList(2, validBundles.size))
    }

    test("Bundle can be updated") {
        val newBundle = validBundles.first().copy(id = validBundles[1].id)
        bundleOperations.updateBundle(newBundle).shouldNotBeNull()
        val updatedBundle = bundleOperations.selectBundleById(validBundles[1].id).shouldNotBeNull()

        updatedBundle.id shouldBe validBundles[1].id
        updatedBundle.name shouldBe validBundles.first().name
        updatedBundle.isActual shouldBe validBundles.first().isActual
    }

    test("Bundle actuality can be updated") {
        val bundleWithUpdatedActuality =
            bundleOperations.updateBundleActuality(validBundles.first().copy(isActual = false)).shouldNotBeNull()

        bundleWithUpdatedActuality.id shouldBe validBundles[0].id
        bundleWithUpdatedActuality.name shouldBe validBundles.first().name
        bundleWithUpdatedActuality.isActual shouldBe !validBundles.first().isActual
    }

    test("Bundle tasks can be updated") {
        val updatedBundleTasks =
            bundleOperations.updateBundleTasks(
                insertedBundles.first().id,
                listOf(insertedBundleTasks[1], insertedBundleTasks[0])
            ).shouldNotBeNull()

        updatedBundleTasks shouldContainExactlyInAnyOrder listOf(insertedBundleTasks[1], insertedBundleTasks[0])
        updatedBundleTasks.size shouldBe 2
    }
})
