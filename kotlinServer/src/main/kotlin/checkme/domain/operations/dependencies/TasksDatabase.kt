package checkme.domain.operations.dependencies

import checkme.domain.checks.Criterion
import checkme.domain.models.AnswerType
import checkme.domain.models.Task

interface TasksDatabase {
    fun selectTaskById(taskId: Int): Task?

    fun selectAllTask(): List<Task>

    fun insertTask(
        name: String,
        criterions: Map<String, Criterion>,
        answerFormat: Map<String, AnswerType>,
        description: String,
    ): Task?

    fun deleteTask(taskId: Int): Int?
}
