package checkme.domain.operations.tasks

import checkme.db.validTaskId
import checkme.db.validTasks
import checkme.domain.checks.Criterion
import checkme.domain.models.AnswerType
import checkme.domain.models.Task
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec

class CreateTaskTest : FunSpec({
    val tasks = mutableListOf<Task>()
    isolationMode = IsolationMode.InstancePerTest

    val insertTaskMock: (
        name: String,
        criterions: Map<String, Criterion>,
        answerFormat: Map<String, AnswerType>,
        description: String,
        isActual: Boolean,
    ) -> Task? = {
            name,
            criterions,
            answerFormat,
            description,
            isActual,
        ->
        val task =
            Task(
                id = validTaskId[0],
                name = name,
                criterions = criterions,
                answerFormat = answerFormat,
                description = description,
                isActual = isActual
            )

        tasks.add(
            Task(
                id = task.id,
                name = name,
                criterions = criterions,
                answerFormat = answerFormat,
                description = description,
                isActual = true
            )
        )
        task
    }
    val insertTaskNullMock: (
        name: String,
        criterions: Map<String, Criterion>,
        answerFormat: Map<String, AnswerType>,
        description: String,
        isActual: Boolean,
    ) -> Task? = { _, _, _, _, _ -> null }

    val createTask = CreateTask(insertTaskMock)

    val createNullTask = CreateTask(
        insertTaskNullMock
    )

    AnswerType.entries.forEach { answerType ->
        test("Valid task with $answerType can be inserted") {
            val validTask = validTasks.first()
            createTask(
                validTask.name,
                validTask.criterions,
                mapOf(answerType.code to answerType),
                validTask.description,
                validTask.isActual
            ).shouldBeSuccess()
        }
    }

    test("Unknown db error test for insert task in bd") {
        val validTask = validTasks.first()
        createNullTask(
            validTask.name,
            validTask.criterions,
            validTask.answerFormat,
            validTask.description,
            validTask.isActual
        ).shouldBeFailure(CreateTaskError.UNKNOWN_DATABASE_ERROR)
    }
})
