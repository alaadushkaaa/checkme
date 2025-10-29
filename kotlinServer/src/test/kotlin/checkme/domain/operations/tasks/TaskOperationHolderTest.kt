package checkme.domain.operations.tasks

import checkme.domain.operations.dependencies.tasks.TasksDatabase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.mock

class TaskOperationHolderTest : FunSpec({
    val tasksOperations: TasksDatabase = mock()
    val taskOperationsHolder = TaskOperationsHolder(tasksOperations)

    test("TaskOperationsHolder should initialize with provided task operations") {
        taskOperationsHolder.fetchTaskById::class.shouldBe(FetchTaskById::class)
        taskOperationsHolder.fetchAllTasks::class.shouldBe(FetchAllTasks::class)
        taskOperationsHolder.fetchHiddenTasks::class.shouldBe(FetchHiddenTasks::class)
        taskOperationsHolder.fetchTaskName::class.shouldBe(FetchTaskName::class)
        taskOperationsHolder.removeTask::class.shouldBe(RemoveTask::class)
        taskOperationsHolder.createTask::class.shouldBe(CreateTask::class)
    }
})
