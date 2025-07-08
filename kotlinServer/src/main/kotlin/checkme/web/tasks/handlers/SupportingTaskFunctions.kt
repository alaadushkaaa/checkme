package checkme.web.tasks.handlers

import checkme.db.tasks.TasksOperations
import checkme.domain.models.Task
import checkme.domain.operations.tasks.CreateTaskError
import checkme.domain.operations.tasks.TaskOperationsHolder
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success

internal fun addTask(
    task: Task,
    taskOperations: TaskOperationsHolder
) : Result<Task, CreateTaskError> {
    return when (
        val newTask = taskOperations.createCheck(
            task.name,

        )
    )
}