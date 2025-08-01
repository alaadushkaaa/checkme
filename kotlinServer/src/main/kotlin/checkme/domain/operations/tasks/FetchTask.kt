package checkme.domain.operations.tasks

import checkme.domain.models.Task
import checkme.web.solution.forms.TaskDataForAllResults
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import org.jooq.exception.DataAccessException

class FetchTaskById(
    private val fetchTaskById: (Int) -> Task?,
) : (Int) -> Result4k<Task, TaskFetchingError> {

    override fun invoke(taskId: Int): Result4k<Task, TaskFetchingError> =
        try {
            when (val task = fetchTaskById(taskId)) {
                is Task -> Success(task)
                else -> Failure(TaskFetchingError.NO_SUCH_TASK)
            }
        } catch (_: DataAccessException) {
            Failure(TaskFetchingError.UNKNOWN_DATABASE_ERROR)
        }
}

class FetchAllTasks(
    private val fetchAllTasks: () -> List<Task>?,
) : () -> Result4k<List<Task>, TaskFetchingError> {

    override fun invoke(): Result4k<List<Task>, TaskFetchingError> =
        try {
            when (val tasks = fetchAllTasks()) {
                is List<Task> -> Success(tasks)
                else -> Failure(TaskFetchingError.NO_SUCH_TASK)
            }
        } catch (_: DataAccessException) {
            Failure(TaskFetchingError.UNKNOWN_DATABASE_ERROR)
        }
}

class FetchTaskName(
    private val fetchTaskName: (taskId: Int) -> TaskDataForAllResults?
) : (Int)  -> Result4k<TaskDataForAllResults, TaskFetchingError> {
    override fun invoke(taskId: Int): Result4k<TaskDataForAllResults, TaskFetchingError> =
        try {
            when (val taskName = fetchTaskName(taskId)) {
                is TaskDataForAllResults -> Success(taskName)
                else -> Failure(TaskFetchingError.NO_SUCH_TASK)
            }
        } catch (_: DataAccessException) {
            Failure(TaskFetchingError.UNKNOWN_DATABASE_ERROR)
        }
}

enum class TaskFetchingError {
    UNKNOWN_DATABASE_ERROR,
    NO_SUCH_TASK,
}
