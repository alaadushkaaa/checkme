package checkme.domain.operations.tasks

import checkme.domain.models.Task
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import org.jooq.exception.DataAccessException

class RemoveTask(
    val removeTask: (Int) -> Unit,
) : (Task) -> Result<Boolean, TaskRemovingError> {
    override fun invoke(task: Task): Result<Boolean, TaskRemovingError> {
        return try {
            removeTask(task.id)
            Success(true)
        } catch (_: DataAccessException) {
            Failure(TaskRemovingError.UNKNOWN_DATABASE_ERROR)
        }
    }
}

enum class TaskRemovingError {
    UNKNOWN_DATABASE_ERROR,
}
