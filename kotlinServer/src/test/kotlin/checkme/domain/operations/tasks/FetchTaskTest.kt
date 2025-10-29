package checkme.domain.operations.tasks

import checkme.db.validTasks
import checkme.domain.models.Task
import checkme.web.solution.forms.TaskNameForAllResults
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class FetchTaskTest : FunSpec({
    val tasks = validTasks
    val task = validTasks.first()
    val taskName = TaskNameForAllResults(task.name)

    val fetchAllTasksMock: () -> List<Task> = { tasks.filter { it.isActual } }
    val fetchHiddenTasksMock: () -> List<Task> = { tasks.filter { !it.isActual } }
    val fetchOneTaskMock: () -> List<Task> = { listOf(task) }
    val fetchTaskByIdMock: (Int) -> Task? = { id -> tasks.firstOrNull { it.id == id } }
    val fetchTaskNameMock: (Int) -> TaskNameForAllResults? = { id ->
        tasks.firstOrNull { it.id == id }
            ?.let { TaskNameForAllResults(it.name) }
    }

    val fetchAllTasks = FetchAllTasks(fetchAllTasksMock)
    val fetchHiddenTasks = FetchHiddenTasks(fetchHiddenTasksMock)
    val fetchOneTaskAsList = FetchAllTasks(fetchOneTaskMock)
    val fetchTaskById = FetchTaskById(fetchTaskByIdMock)
    val fetchTaskName = FetchTaskName(fetchTaskNameMock)

    test("Fetch all tasks should return list of tasks and list have size more than one") {
        fetchAllTasks().shouldBeSuccess().shouldHaveSize(tasks.filter { it.isActual }.size)
    }

    test("Fetch hidden tasks should return tasks with isActual = false") {
        fetchHiddenTasks().shouldBeSuccess().shouldHaveSize(tasks.filter { !it.isActual }.size)
    }

    test("Fetch all tasks but only one task exists should return list of tasks and list have size one") {
        fetchOneTaskAsList().shouldBeSuccess().shouldHaveSize(1)
    }

    test("Fetch Task by valid task id") {
        fetchTaskById(task.id).shouldBeSuccess() shouldBe task
    }

    test("Fetch Task by id should return an error if id is not valid") {
        fetchTaskById(tasks.maxOf { it.id } + 1).shouldBeFailure(TaskFetchingError.NO_SUCH_TASK)
    }

    test("Fetch task name by task id should return task name if id is valid") {
        fetchTaskName(task.id).shouldBeSuccess() shouldBe taskName
    }

    test("Fetch task name by task id should return an error if id is not valid") {
        fetchTaskName(tasks.maxOf { it.id } + 1).shouldBeFailure(TaskFetchingError.NO_SUCH_TASK)
    }
})
