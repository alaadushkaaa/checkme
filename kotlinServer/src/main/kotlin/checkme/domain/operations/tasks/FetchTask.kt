package checkme.domain.operations.tasks

import checkme.domain.models.Task
import checkme.web.solution.forms.TaskIdAndName
import checkme.web.solution.forms.TaskNameForAllResults
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

class FetchHiddenTasks(
    private val fetchHiddenTasks: () -> List<Task>?,
) : () -> Result4k<List<Task>, TaskFetchingError> {

    override fun invoke(): Result4k<List<Task>, TaskFetchingError> =
        try {
            when (val tasks = fetchHiddenTasks()) {
                is List<Task> -> Success(tasks)
                else -> Failure(TaskFetchingError.NO_SUCH_TASK)
            }
        } catch (_: DataAccessException) {
            Failure(TaskFetchingError.UNKNOWN_DATABASE_ERROR)
        }
}

class FetchAllTasksIdAndName(
    private val fetchAllTasksIdAndName: () -> List<TaskIdAndName>?,
) : () -> Result4k<List<TaskIdAndName>, TaskFetchingError> {
    override fun invoke(): Result4k<List<TaskIdAndName>, TaskFetchingError> =
        try {
            when (val tasks = fetchAllTasksIdAndName()) {
                is List<TaskIdAndName> -> Success(tasks)
                else -> Failure(TaskFetchingError.NO_SUCH_TASK)
            }
        } catch (_: DataAccessException) {
            Failure(TaskFetchingError.UNKNOWN_DATABASE_ERROR)
        }
}

class FetchAllTasksPagination(
    private val fetchAllTasksWithData: (Int) -> List<Task>?,
) : (Int) -> Result4k<List<Task>, TaskFetchingError> {

    override fun invoke(page: Int): Result4k<List<Task>, TaskFetchingError> =
        try {
            when (val tasks = fetchAllTasksWithData(page)) {
                is List<Task> -> Success(tasks)
                else -> Failure(TaskFetchingError.NO_SUCH_TASK)
            }
        } catch (_: DataAccessException) {
            Failure(TaskFetchingError.UNKNOWN_DATABASE_ERROR)
        }
}

class FetchTaskName(
    private val fetchTaskName: (taskId: Int) -> TaskNameForAllResults?,
) : (Int) -> Result4k<TaskNameForAllResults, TaskFetchingError> {
    override fun invoke(taskId: Int): Result4k<TaskNameForAllResults, TaskFetchingError> =
        try {
            when (val taskName = fetchTaskName(taskId)) {
                is TaskNameForAllResults -> Success(taskName)
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
