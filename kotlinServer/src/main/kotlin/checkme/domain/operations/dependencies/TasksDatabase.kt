package checkme.domain.operations.dependencies

import checkme.domain.checks.Criterion
import checkme.domain.models.Task

interface TasksDatabase {
    fun selectTaskById(taskId: Int): Task?

    fun selectAllTask(): List<Task>

    fun insertTask(
        taskId: Int,
        name: String,
        criterions: Map<String, Criterion>,
        answerFormat: String,
        description: String,
    ): Task?
}
