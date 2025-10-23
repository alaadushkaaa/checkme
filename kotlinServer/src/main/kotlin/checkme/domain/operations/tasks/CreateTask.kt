package checkme.domain.operations.tasks

import checkme.domain.checks.Criterion
import checkme.domain.models.AnswerType
import checkme.domain.models.Task
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success

class CreateTask(
    private val insertTask: (
        name: String,
        criterions: Map<String, Criterion>,
        answerFormat: Map<String, AnswerType>,
        description: String,
        isActual: Boolean,
    ) -> Task?,
) : (
        String,
        Map<String, Criterion>,
        Map<String, AnswerType>,
        String,
        Boolean,
    ) -> Result4k<Task, CreateTaskError> {
    override fun invoke(
        name: String,
        criterions: Map<String, Criterion>,
        answerFormat: Map<String, AnswerType>,
        description: String,
        isActual: Boolean,
    ): Result4k<Task, CreateTaskError> =
        when (
            val newTask = insertTask(
                name,
                criterions,
                answerFormat,
                description,
                isActual
            )
        ) {
            is Task -> Success(newTask)
            else -> Failure(CreateTaskError.UNKNOWN_DATABASE_ERROR)
        }
}

enum class CreateTaskError {
    UNKNOWN_DATABASE_ERROR,
}
