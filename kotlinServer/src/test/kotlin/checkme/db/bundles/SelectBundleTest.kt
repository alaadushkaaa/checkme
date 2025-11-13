package checkme.db.bundles

import checkme.db.TestcontainerSpec
import checkme.db.tasks.TasksOperations
import checkme.db.validBundleTasks
import checkme.db.validBundles
import checkme.db.validTasks
import checkme.domain.models.Bundle
import checkme.domain.models.TaskAndOrder
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class SelectBundleTest : TestcontainerSpec({ context ->
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

    test("Select bundle by id should return this bundle") {
        val selectedBundle = bundleOperations.selectBundleById(insertedBundles.first().id).shouldNotBeNull()

        selectedBundle.id.shouldBe(insertedBundles.first().id)
        selectedBundle.name.shouldBe(insertedBundles.first().name)
        selectedBundle.isActual.shouldBe(true)
    }

    test("Select bundle by invalid id should return null") {
        bundleOperations.selectBundleById(insertedBundles.maxOf { it.id } + 1).shouldBeNull()
    }

    test("Select all bundles should return all of this inserted bundles") {
        val selectedBundles = bundleOperations.selectAllBundles().shouldNotBeNull()
        selectedBundles shouldContainExactlyInAnyOrder insertedBundles
    }

    test("Select hidden bundles from db") {
        bundleOperations
            .selectHiddenBundles()
            .shouldBe(insertedBundles.filter { !it.isActual })
    }

    test("Select bundle tasks should return all of this inserted bundle tasks") {
        val selectedTasks = bundleOperations.selectBundleTasksById(insertedBundles.first().id).shouldNotBeNull()
        selectedTasks shouldContainExactlyInAnyOrder insertedBundleTasks
    }
})
