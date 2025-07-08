package checkme.domain.operations.tasks

import checkme.domain.models.Task
import checkme.domain.operations.dependencies.TasksDatabase
import dev.forkhandles.result4k.Result

class TaskOperationsHolder (
    private val tasksDatabase: TasksDatabase,
) {
    val fetchTaskById: (Int) -> Result<Task, CreateTaskError> =
        FetchTaskById {
                taskId: Int,
            ->
            tasksDatabase.selectTaskById(taskId)
        }
}