package checkme.web.tasks.handlers

import checkme.db.tasks.TasksOperations
import checkme.domain.models.Task

internal fun addTask(
    task: Task,
    taskOperationsHolder: TasksOperations
) : Result<Task, CreationTaskError> {
    return when (
        val newTask =
    )
}