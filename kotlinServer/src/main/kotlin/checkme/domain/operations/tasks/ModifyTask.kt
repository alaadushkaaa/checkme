package checkme.domain.operations.tasks

import checkme.domain.models.Task
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import org.jooq.exception.DataAccessException
import java.util.UUID

class RemoveTask(
    private val selectTaskById: (taskId: UUID) -> Task?,
    private val removeTask: (UUID) -> Int?,
) : (Task) -> Result<UUID?, TaskRemovingError> {
    override fun invoke(task: Task): Result<UUID?, TaskRemovingError> {
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

    private fun taskNotExists(taskId: UUID): Boolean =
        when (selectTaskById(taskId)) {
            is Task -> false
            else -> true
        }
}

class ModifyTaskActuality(
    private val updateTaskActuality: (
        task: Task,
    ) -> Task?,
) : (Task) -> Result<Task, ModifyTaskError> {
    override operator fun invoke(task: Task): Result<Task, ModifyTaskError> =
        when (
            val editedTask = updateTaskActuality(
                task
            )
        ) {
            is Task -> Success(editedTask)
            else -> {
                Failure(ModifyTaskError.UNKNOWN_DATABASE_ERROR)
            }
        }
}

enum class TaskRemovingError {
    UNKNOWN_DATABASE_ERROR,
    UNKNOWN_DELETE_ERROR,
    TASK_NOT_EXISTS,
}

enum class ModifyTaskError {
    UNKNOWN_DATABASE_ERROR,
}
