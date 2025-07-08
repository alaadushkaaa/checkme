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
): Result<Task, CreationTaskError> {
    return when (
        val newTask = taskOperations.createCheck(
            task.name,
            task.criterions,
            task.answerFormat,
            task.description,
        )
    ) {
        is Success -> Success(newTask.value)
        is Failure -> when (newTask.reason) {
            CreateTaskError.UNKNOWN_DATABASE_ERROR -> Failure(CreationTaskError.UNKNOWN_DATABASE_ERROR)
        }
    }
}

enum class CreationTaskError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Something happened. Please try again later or ask for help"),
}