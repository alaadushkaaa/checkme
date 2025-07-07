package checkme.db.tasks

import checkme.db.TestcontainerSpec
import checkme.db.validTasks
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class SelectTaskTest : TestcontainerSpec ({ context ->
    val taskOperations = TasksOperations(context)

    beforeEach {
        for (task in validTasks) {
            taskOperations.insertTask(
                task.name,
                task.criterions,
                task.answerFormat,
                task.description
            ).shouldNotBeNull()
        }
    }

    test("Select all tasks from db") {
        taskOperations
            .selectAllTask()
            .shouldBe(validTasks)
    }

    test("Select task by valid id") {
        val fetchedTask =
            taskOperations
                .selectTaskById(validTasks.first().id)
                .shouldNotBeNull()

        fetchedTask.name.shouldBe(validTasks.first().name)
        fetchedTask.criterions.shouldBe(validTasks.first().criterions)
        fetchedTask.answerFormat.shouldBe(validTasks.first().answerFormat)
        fetchedTask.description.shouldBe(validTasks.first().description)
    }

    test("Task cant be fetched by invalid id") {
        taskOperations
            .selectTaskById(validTasks.first().id + validTasks.size)
            .shouldBeNull()
    }
})
