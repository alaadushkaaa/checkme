package checkme.domain.operations.tasks

import checkme.domain.checks.Criterion
import checkme.domain.forms.CheckResult
import checkme.domain.models.Check
import checkme.domain.models.FormatOfAnswer
import checkme.domain.models.Task
import checkme.domain.operations.checks.CreateCheck
import checkme.domain.operations.checks.CreateCheckError
import checkme.domain.operations.dependencies.TasksDatabase
import dev.forkhandles.result4k.Result
import java.time.LocalDateTime

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

    val createCheck: (
        name: String,
        criterions: Map<String, Criterion>,
        answerFormat: FormatOfAnswer,
        description: String,
    ) -> Result<Task, CreateTaskError> =
        CreateTask {
                name: String,
                criterions: Map<String, Criterion>,
                answerFormat: FormatOfAnswer,
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
