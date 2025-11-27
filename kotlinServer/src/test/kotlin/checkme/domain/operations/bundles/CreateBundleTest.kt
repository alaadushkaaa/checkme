package checkme.domain.operations.bundles

import checkme.db.validBundleTasks
import checkme.db.validBundles
import checkme.domain.models.Bundle
import checkme.domain.models.TaskAndOrder
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec

class CreateBundleTest : FunSpec({
    val bundles = mutableListOf<Bundle>()
    val bundlesTasks = mutableListOf<TaskAndOrder>()
    isolationMode = IsolationMode.InstancePerTest

    val insertBundleMock: (
        name: String,
    ) -> Bundle? = { name,
        ->
        val bundle =
            Bundle(
                id = bundles.size + 1,
                name = name,
                isActual = true
            )

        bundles.add(
            Bundle(
                id = bundle.id,
                name = name,
                isActual = true
            )
        )
        bundle
    }
    val insertBundleNullMock: (
        name: String,
    ) -> Bundle? = { null }

    val insertBundleTasksMock: (
        bundleId: Int,
        tasksAndOrder: List<TaskAndOrder>,
    ) -> List<TaskAndOrder>? = {
            bundleId,
            taskAndOrder,
        ->
        taskAndOrder.forEach { task ->
            if (bundles.map { it.id }.contains(bundleId)) {
                val bundleTask =
                    TaskAndOrder(
                        task = task.task,
                        order = task.order
                    )
                bundlesTasks.add(bundleTask)
            }
        }

        taskAndOrder
    }

    val insertBundleTasksNullMock: (
        bundleId: Int,
        tasksAndOrder: List<TaskAndOrder>,
    ) -> List<TaskAndOrder>? = { _, _ -> null }

    val selectBundleMock: (
        bundleId: Int,
    ) -> Bundle? = { bundleId ->
        when (val existedBundle: Bundle? = bundles.firstOrNull { it.id == bundleId }) {
            is Bundle -> existedBundle
            else -> null
        }
    }

    val createBundle = CreateBundle(insertBundleMock)

    val createNullBundle = CreateBundle(
        insertBundleNullMock
    )

    val createBundleTasks = CreateBundleTasks(selectBundleMock, insertBundleTasksMock)
    val createBundleTasksNull = CreateBundleTasks(selectBundleMock, insertBundleTasksNullMock)

    test("Task with valid name can be inserted") {
        val validBundle = validBundles.first()
        createBundle(validBundle.name).shouldBeSuccess()
    }

    test("Unknown db error test for insert bundle") {
        val validBundle = validBundles.first()
        createNullBundle(validBundle.name).shouldBeFailure(CreateBundleError.UNKNOWN_DATABASE_ERROR)
    }

    test("Valid bundle tasks can be inserted") {
        createBundle(validBundles.first().name)
        createBundleTasks(bundles.first().id, validBundleTasks).shouldBeSuccess()
    }

    test("Unknown db error test for insert bundle tasks") {
        createBundle(validBundles.first().name)
        createBundleTasksNull(bundles.first().id, validBundleTasks)
            .shouldBeFailure(CreateBundleTasksError.UNKNOWN_DATABASE_ERROR)
    }

    test("Cant insert bundle tasks when bundle not exists") {
        createBundle(validBundles.first().name)
        createBundleTasks(bundles.size + 1, validBundleTasks)
            .shouldBeFailure(CreateBundleTasksError.NO_SUCH_BUNDLE_FOR_TASKS)
    }

    test("Cant insert bundle tasks when tasks list is empty") {
        createBundle(validBundles.first().name)
        createBundleTasks(bundles.first().id, emptyList())
            .shouldBeFailure(CreateBundleTasksError.TASKS_LIST_IS_EMPTY)
    }
})
