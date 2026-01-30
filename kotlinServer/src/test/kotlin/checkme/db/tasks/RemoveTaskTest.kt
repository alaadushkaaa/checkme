package checkme.db.tasks

import checkme.db.TaskWithoutId
import checkme.db.TestcontainerSpec
import checkme.db.notExistingId
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
        val validFirstTaskId =
            taskOperations
                .selectAllTask().first().id
        taskOperations.deleteTask(validFirstTaskId).shouldBe(1)
    }

    test("Only one task can be deleted") {
        val taskForRemove = taskOperations.selectAllTask().map { it.id }.first()
        val secondTaskForRemove = taskOperations.selectHiddenTasks().map { it.id }.first()
        taskOperations.deleteTask(taskForRemove).shouldBe(1)
        taskOperations.selectAllTask().map {
            if (it.id.toString().matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$".toRegex())) {
                TaskWithoutId(it.name, it.criterions, it.answerFormat, it.description, it.isActual)
            } else {
                null
            }
        }.shouldBe(validTasks.subList(2, validTasks.size).map {
            if (it.id.toString().matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$".toRegex())) {
                TaskWithoutId(it.name, it.criterions, it.answerFormat, it.description, it.isActual)
            } else {
                null
            }
        })
        taskOperations.deleteTask(secondTaskForRemove).shouldBe(1)
        taskOperations.selectAllTask().map {
            if (it.id.toString().matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$".toRegex())) {
                TaskWithoutId(it.name, it.criterions, it.answerFormat, it.description, it.isActual)
            } else {
                null
            }
        }.shouldBe(validTasks.subList(2, validTasks.size).map {
            if (it.id.toString().matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$".toRegex())) {
                TaskWithoutId(it.name, it.criterions, it.answerFormat, it.description, it.isActual)
            } else {
                null
            }
        })
    }

    test("Cant delete task by invalid id") {
        taskOperations.deleteTask(notExistingId).shouldBe(0)
    }

    test("Task actuality can bu updated") {
        val firstTaskFromDB = taskOperations.selectAllTask().first()
        val updatedTask =
            taskOperations.updateTaskActuality(firstTaskFromDB.copy(isActual = false)).shouldNotBeNull()

        updatedTask.name.shouldBe(validTasks.first().name)
        updatedTask.criterions.shouldBe(validTasks.first().criterions)
        updatedTask.answerFormat.shouldBe(validTasks.first().answerFormat)
        updatedTask.description.shouldBe(validTasks.first().description)
        updatedTask.isActual.shouldBe(!validTasks.first().isActual)
    }
})
