package checkme.domain.operations.tasks

import checkme.domain.checks.Criterion
import checkme.domain.models.AnswerType
import checkme.domain.models.Task
import checkme.domain.operations.dependencies.tasks.TasksDatabase
import checkme.web.solution.forms.TaskIdAndName
import checkme.web.solution.forms.TaskNameForAllResults
import dev.forkhandles.result4k.Result
import java.util.UUID

class TaskOperationsHolder (
    private val tasksDatabase: TasksDatabase,
) {
    val fetchTaskById: (UUID) -> Result<Task, TaskFetchingError> =
        FetchTaskById {
                taskId: UUID,
            ->
            tasksDatabase.selectTaskById(taskId)
        }

    val fetchAllTasks: () -> Result<List<Task>, TaskFetchingError> =
        FetchAllTasks {
            tasksDatabase.selectAllTask()
        }

    val fetchHiddenTasks: () -> Result<List<Task>, TaskFetchingError> =
        FetchHiddenTasks {
            tasksDatabase.selectHiddenTasks()
        }

    val fetchAllTasksIdAndName: () -> Result<List<TaskIdAndName>, TaskFetchingError> =
        FetchAllTasksIdAndName {
            tasksDatabase.selectAllTasksIdAndNames()
        }

    val fetchAllTasksPagination: (Int) -> Result<List<Task>, TaskFetchingError> =
        FetchAllTasksPagination {
                page: Int ->
            tasksDatabase.selectAllTasksPagination(page)
        }

    val fetchTaskName: (UUID) -> Result<TaskNameForAllResults, TaskFetchingError> =
        FetchTaskName {
                taskId: UUID ->
            tasksDatabase.selectTaskName(taskId)
        }

    val removeTask: (task: Task) -> Result<UUID?, TaskRemovingError> =
        RemoveTask(
            selectTaskById = tasksDatabase::selectTaskById,
            removeTask = tasksDatabase::deleteTask
        )

    val updateTaskActuality: (
        task: Task,
    ) -> Result<Task, ModifyTaskError> =
        ModifyTaskActuality {
                task,
            ->
            tasksDatabase.updateTaskActuality(task)
        }

    val createTask: (
        name: String,
        criterions: Map<String, Criterion>,
        answerFormat: Map<String, AnswerType>,
        description: String,
        isActual: Boolean,
    ) -> Result<Task, CreateTaskError> =
        CreateTask {
                name: String,
                criterions: Map<String, Criterion>,
                answerFormat: Map<String, AnswerType>,
                description: String,
                isActual: Boolean,
            ->
            tasksDatabase.insertTask(
                name,
                criterions,
                answerFormat,
                description,
                isActual
            )
        }
}
