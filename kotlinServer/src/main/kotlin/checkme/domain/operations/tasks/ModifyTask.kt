package checkme.domain.operations.tasks

import checkme.domain.models.Task
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import org.jooq.exception.DataAccessException

class RemoveTask(
    private val selectTaskById: (taskId: Int) -> Task?,
    private val removeTask: (Int) -> Int?,
) : (Task) -> Result<Int, TaskRemovingError> {
    override fun invoke(task: Task): Result<Int, TaskRemovingError> {
        return try {
            when {
                taskNotExists(task.id) -> Failure(TaskRemovingError.TASK_NOT_EXISTS)
                else -> when (removeTask(task.id)) {
                    is Int -> Success(task.id)
                    else -> Failure(TaskRemovingError.UNKNOWN_DELETE_ERROR)
                }
            }
        } catch (_: DataAccessException) {
            Failure(TaskRemovingError.UNKNOWN_DATABASE_ERROR)
        }
    }

    private fun taskNotExists(taskId: Int): Boolean =
        when (selectTaskById(taskId)) {
            is Task -> false
            else -> true
        }
}

enum class TaskRemovingError {
    UNKNOWN_DATABASE_ERROR,
    UNKNOWN_DELETE_ERROR,
    TASK_NOT_EXISTS,
}
