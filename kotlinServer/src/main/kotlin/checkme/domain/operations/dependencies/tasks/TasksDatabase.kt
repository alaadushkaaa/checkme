package checkme.domain.operations.dependencies.tasks

import checkme.domain.checks.Criterion
import checkme.domain.models.AnswerType
import checkme.domain.models.Task
import checkme.web.solution.forms.TaskIdAndName
import checkme.web.solution.forms.TaskNameForAllResults

interface TasksDatabase {
    fun selectTaskById(taskId: Int): Task?

    fun selectAllTask(): List<Task>

    fun selectHiddenTasks(): List<Task>

    fun selectAllTasksIdAndNames(): List<TaskIdAndName>

    fun selectTaskName(taskId: Int): TaskNameForAllResults?

    fun updateTaskActuality(task: Task): Task?

    fun insertTask(
        name: String,
        criterions: Map<String, Criterion>,
        answerFormat: Map<String, AnswerType>,
        description: String,
        isActual: Boolean,
    ): Task?

    fun deleteTask(taskId: Int): Int
}
