package checkme.domain.operations.tasks

import checkme.domain.checks.Criterion
import checkme.domain.models.AnswerType
import checkme.domain.models.Task
import checkme.domain.operations.dependencies.tasks.TasksDatabase
import checkme.web.solution.forms.TaskDataForAllResults
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

    val fetchTaskName: (Int) -> Result<TaskDataForAllResults, TaskFetchingError> =
        FetchTaskName {
                taskId: Int ->
            tasksDatabase.selectTaskName(taskId)
        }

    val removeTask: (task: Task) -> Result<Int, TaskRemovingError> =
        RemoveTask(
            selectTaskById = tasksDatabase::selectTaskById,
            removeTask = tasksDatabase::deleteTask
        )

    val createTask: (
        name: String,
        criterions: Map<String, Criterion>,
        answerFormat: Map<String, AnswerType>,
        description: String,
    ) -> Result<Task, CreateTaskError> =
        CreateTask {
                name: String,
                criterions: Map<String, Criterion>,
                answerFormat: Map<String, AnswerType>,
                description: String,
            ->
            tasksDatabase.insertTask(
                name,
                criterions,
                answerFormat,
                description
            )
        }
}
