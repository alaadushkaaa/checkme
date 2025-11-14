package checkme.db.tasks

import checkme.db.TestcontainerSpec
import checkme.db.validTasks
import checkme.web.solution.forms.TaskIdAndName
import checkme.web.solution.forms.TaskNameForAllResults
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
                task.description,
                task.isActual
            ).shouldNotBeNull()
        }
    }

    test("Select all tasks from db") {
        taskOperations
            .selectAllTask()
            .shouldBe(validTasks.filter { it.isActual })
    }

    test("Select all tasks ids and names from db") {
        taskOperations
            .selectAllTasksIdAndNames()
            .shouldBe(validTasks.map { task -> TaskIdAndName(task.id.toString(), task.name) })
    }

    test("Select hidden tasks from db") {
        taskOperations
            .selectHiddenTasks()
            .shouldBe(validTasks.filter { !it.isActual })
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
        fetchedTask.isActual.shouldBe(true)
    }

    test("Task cant be fetched by invalid id") {
        taskOperations
            .selectTaskById(validTasks.first().id + validTasks.size)
            .shouldBeNull()
    }

    test("Select task name should return entity with only task name") {
        taskOperations
            .selectTaskName(validTasks.first().id)
            .shouldNotBeNull()
            .shouldBe(TaskNameForAllResults(validTasks.first().name))
    }

    test("Task name cant be fetched by invalid task id") {
        taskOperations
            .selectTaskName(validTasks.first().id + validTasks.size)
            .shouldBeNull()
    }
})
