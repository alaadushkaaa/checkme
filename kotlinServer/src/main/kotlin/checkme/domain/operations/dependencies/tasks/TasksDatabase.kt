package checkme.domain.operations.dependencies.tasks

import checkme.domain.checks.Criterion
import checkme.domain.models.AnswerType
import checkme.domain.models.Task
import checkme.web.solution.forms.TaskNameForAllResults

interface TasksDatabase {
    fun selectTaskById(taskId: Int): Task?

    fun selectAllTask(): List<Task>

    fun selectTaskName(taskId: Int): TaskNameForAllResults?

    fun insertTask(
        name: String,
        criterions: Map<String, Criterion>,
        answerFormat: Map<String, AnswerType>,
        description: String,
    ): Task?

    fun deleteTask(taskId: Int): Int?
}
