package checkme.db.tasks

import checkme.db.TestcontainerSpec
import checkme.db.validTasks
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class RemoveTaskTest : TestcontainerSpec({ context ->
    val taskOperations = TasksOperations(context)

    beforeEach {
        for (task in validTasks) {
            taskOperations.insertTask(
                task.name,
                task.criterions,
                task.answerFormat,
                task.description,
                task.isActual
            ).shouldNotBeNull()
        }
    }

    test("Valid task can be removed") {
        val taskForRemove = validTasks.first()
        taskOperations.deleteTask(taskForRemove.id).shouldBe(1)
    }

    test("Only one task can be deleted") {
        val taskForRemove = validTasks.first()
        taskOperations.deleteTask(taskForRemove.id).shouldBe(1)
        taskOperations.selectAllTask().shouldBe(validTasks.subList(2, validTasks.size))
        taskOperations.deleteTask(validTasks[1].id).shouldBe(1)
        taskOperations.selectAllTask().shouldBe(validTasks.subList(2, validTasks.size))
    }

    test("Cant delete task by invalid id") {
        taskOperations.deleteTask(validTasks.size + 1).shouldBe(0)
    }

    test("Task actuality can bu updated") {
        val updatedTask =
            taskOperations.updateTaskActuality(validTasks.first().copy(isActual = false)).shouldNotBeNull()

        updatedTask.name.shouldBe(validTasks.first().name)
        updatedTask.criterions.shouldBe(validTasks.first().criterions)
        updatedTask.answerFormat.shouldBe(validTasks.first().answerFormat)
        updatedTask.description.shouldBe(validTasks.first().description)
        updatedTask.isActual.shouldBe(!validTasks.first().isActual)
    }
})
