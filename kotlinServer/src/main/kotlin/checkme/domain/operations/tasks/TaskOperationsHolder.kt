package checkme.domain.operations.tasks

import checkme.domain.checks.Criterion
import checkme.domain.models.AnswerType
import checkme.domain.models.Task
import checkme.domain.operations.dependencies.tasks.TasksDatabase
import checkme.web.solution.forms.TaskNameForAllResults
import dev.forkhandles.result4k.Result

class TaskOperationsHolder (
    private val tasksDatabase: TasksDatabase,
) {
    val fetchTaskById: (Int) -> Result<Task, TaskFetchingError> =
        FetchTaskById {
                taskId: Int,
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

    val fetchTaskName: (Int) -> Result<TaskNameForAllResults, TaskFetchingError> =
        FetchTaskName {
                taskId: Int ->
            tasksDatabase.selectTaskName(taskId)
        }

    val removeTask: (task: Task) -> Result<Int, TaskRemovingError> =
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
