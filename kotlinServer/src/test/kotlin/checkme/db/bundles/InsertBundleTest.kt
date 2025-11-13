package checkme.db.bundles

import checkme.db.TestcontainerSpec
import checkme.db.tasks.TasksOperations
import checkme.db.validBundleTasks
import checkme.db.validBundles
import checkme.db.validTasks
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class InsertBundleTest : TestcontainerSpec({ context ->
    val tasksOperations = TasksOperations(context)
    val bundleOperations = BundleOperations(context, tasksOperations)

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
    }

    test("Valid bundle insertions should return this bundle") {
        val bundleForInsert = validBundles.first()
        val insertedBundle =
            bundleOperations.insertBundle(
                bundleForInsert.name,
            ).shouldNotBeNull()

        insertedBundle.name.shouldBe(bundleForInsert.name)
        insertedBundle.isActual.shouldBe(true)
    }

    test("Valid bundle tasks can be inserted") {
        val bundleForInsert = validBundles.first()
        val insertedBundle =
            bundleOperations.insertBundle(
                bundleForInsert.name,
            ).shouldNotBeNull()
        val insertedTasks = bundleOperations
            .insertBundleTasks(insertedBundle.id, validBundleTasks).shouldNotBeNull()
        insertedTasks.shouldContainExactlyInAnyOrder(validBundleTasks)
    }
})
