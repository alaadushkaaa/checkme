package checkme.db.tasks

import checkme.db.TestcontainerSpec
import checkme.db.validTask
import checkme.domain.models.FormatOfAnswer
import checkme.domain.models.Task
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class SelectTaskTest : TestcontainerSpec ({ context ->
    val taskOperations = TasksOperations(context)
    lateinit var insertedTask: Task

    beforeEach {
        insertedTask =
            taskOperations.insertTask(
                validTask.name,
                validTask.criterions,
                validTask.answerFormat,
                validTask.description
            ).shouldNotBeNull()
    }

    test("Select all tasks from db") {
        taskOperations.insertTask(
            validTask.name,
            validTask.criterions,
            FormatOfAnswer.TEXT,
            validTask.description
        ).shouldNotBeNull()

        taskOperations
            .selectAllTask()
            .shouldNotBeNull()
            .size
            .shouldBe(2)
    }

    test("Select task by valid id") {
        val fetchedTask =
            taskOperations
                .selectTaskById(validTask.id)
                .shouldNotBeNull()

        fetchedTask.name.shouldBe(validTask.name)
        fetchedTask.criterions.shouldBe(validTask.criterions)
        fetchedTask.answerFormat.shouldBe(validTask.answerFormat)
        fetchedTask.description.shouldBe(validTask.description)
    }

    test("Task cant be fetched by invalid id") {
        taskOperations
            .selectTaskById(validTask.id + 1)
            .shouldBeNull()
    }
})
