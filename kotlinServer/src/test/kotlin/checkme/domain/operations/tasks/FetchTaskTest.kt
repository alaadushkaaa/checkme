package checkme.domain.operations.tasks

import checkme.db.validTasks
import checkme.domain.models.Task
import checkme.web.solution.forms.TaskNameForAllResults
import checkme.web.tasks.forms.TasksListData
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class FetchTaskTest : FunSpec({
    val tasks = validTasks
    val task = validTasks.first()
    val tasksListData : List<TasksListData> = validTasks.map {TasksListData(it.id.toString(), it.name)}
    val taskName = TaskNameForAllResults(task.name)

    val fetchAllTasksMock: () -> List<Task> = { tasks }
    val fetchOneTaskMock: () -> List<Task> = { listOf(task) }
    val fetchTaskByIdMock: (Int) -> Task? = {id -> tasks.firstOrNull { it.id == id }}
    val fetchAllTasksIdNameMock: () -> List<TasksListData> = {tasksListData}
    val fetchTaskNameMock: (Int) -> TaskNameForAllResults? = { id -> tasks.firstOrNull { it.id == id }
        ?.let { TaskNameForAllResults(it.name ) } }

    val fetchAllTasks = FetchAllTasks(fetchAllTasksMock)
    val fetchOneTaskAsList = FetchAllTasks(fetchOneTaskMock)
    val fetchTaskById = FetchTaskById(fetchTaskByIdMock)
    val fetchAllTasksIdAndName = FetchAllTasksIdAndName(fetchAllTasksIdNameMock)
    val fetchTaskName = FetchTaskName(fetchTaskNameMock)

    test("Fetch all tasks should return list of tasks and list have size more than one") {
        fetchAllTasks().shouldBeSuccess().shouldHaveSize(tasks.size)
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

    test("Fetch all tasks ids and names should return list with ids and names with size of tasks") {
        fetchAllTasksIdAndName().shouldBeSuccess().shouldHaveSize(validTasks.size) shouldBe tasksListData
    }

    test("Fetch task name by task id should return task name if id is valid") {
        fetchTaskName(task.id).shouldBeSuccess() shouldBe taskName
    }

    test("Fetch task name by task id should return an error if id is not valid") {
        fetchTaskName(tasks.maxOf { it.id } + 1).shouldBeFailure(TaskFetchingError.NO_SUCH_TASK)
    }
})
