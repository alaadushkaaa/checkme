package checkme.domain.operations.tasks

import checkme.db.validTasks
import checkme.domain.models.Task
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec

class ModifyTaskTest : FunSpec({
    isolationMode = IsolationMode.InstancePerTest

    val validTasks = validTasks

    val selectTaskByIdMock: (Int) -> Task? = { id -> validTasks.find { it.id == id } }
    val selectTaskByIdNullMock: (Int) -> Task? = { null }
    val deleteTaskMock: (Int) -> Int? = { id -> validTasks.find { it.id == id }?.id }
    val deleteNullTaskMock: (Int) -> Int? = { null }

    val deleteTask = RemoveTask(selectTaskByIdMock, deleteTaskMock)
    val deleteTaskIdNotExist = RemoveTask(selectTaskByIdNullMock, deleteNullTaskMock)
    val deleteTaskNullDelete = RemoveTask(selectTaskByIdMock, deleteNullTaskMock)

    test("Task can be deleted if task exists") {
        deleteTask(validTasks.first()).shouldBeSuccess()
    }

    test("Task cant be deleted if task doesn't exists") {
        deleteTaskIdNotExist(
            validTasks.first()
                .copy(id = validTasks.maxOf { it.id } + 1)
        ).shouldBeFailure(TaskRemovingError.TASK_NOT_EXISTS)
    }

    test("Task cant be deleted if operations remove task returns null") {
        deleteTaskNullDelete(validTasks.first()).shouldBeFailure(TaskRemovingError.UNKNOWN_DELETE_ERROR)
    }
})
