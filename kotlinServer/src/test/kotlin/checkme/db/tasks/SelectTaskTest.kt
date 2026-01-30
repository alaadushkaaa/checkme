package checkme.db.tasks

import checkme.db.TaskName
import checkme.db.TaskWithoutId
import checkme.db.TestcontainerSpec
import checkme.db.notExistingId
import checkme.db.validTasks
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
            .map {
                if (it.id.toString().matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$".toRegex())) {
                    TaskWithoutId(it.name, it.criterions, it.answerFormat, it.description, it.isActual)
                } else {
                    null
                }
            }
            .shouldBe(validTasks.map {
                TaskWithoutId(
                    it.name,
                    it.criterions,
                    it.answerFormat,
                    it.description,
                    it.isActual
                )
            }.filter { it.isActual })
    }

    test("Select all tasks ids and names from db") {
        taskOperations
            .selectAllTasksIdAndNames()
            .map {
                if (it.id.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$".toRegex())) {
                    TaskName(it.name)
                } else {
                    null
                }
            }
            .shouldBe(validTasks.map { task -> TaskName(task.name) })
    }

    test("Select hidden tasks from db") {
        taskOperations
            .selectHiddenTasks()
            .map {
                if (it.id.toString().matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$".toRegex())) {
                    TaskWithoutId(it.name, it.criterions, it.answerFormat, it.description, it.isActual)
                } else {
                    null
                }
            }
            .shouldBe(validTasks.map {
                TaskWithoutId(
                    it.name,
                    it.criterions,
                    it.answerFormat,
                    it.description,
                    it.isActual
                )
            }.filter { !it.isActual })
    }

    test("Select task by valid id") {
        val validFirstTaskId =
            taskOperations
                .selectAllTask().first().id
        val fetchedTask =
            taskOperations
                .selectTaskById(validFirstTaskId)
                .shouldNotBeNull()

        fetchedTask.name.shouldBe(validTasks.first().name)
        fetchedTask.criterions.shouldBe(validTasks.first().criterions)
        fetchedTask.answerFormat.shouldBe(validTasks.first().answerFormat)
        fetchedTask.description.shouldBe(validTasks.first().description)
        fetchedTask.isActual.shouldBe(true)
    }

    test("Task cant be fetched by invalid id") {
        taskOperations
            .selectTaskById(notExistingId)
            .shouldBeNull()
    }

    test("Select task name should return entity with only task name") {
        val validFirstTaskId =
            taskOperations
                .selectAllTask().first().id
        taskOperations
            .selectTaskName(validFirstTaskId)
            .shouldNotBeNull()
            .shouldBe(TaskNameForAllResults(validTasks.first().name))
    }

    test("Task name cant be fetched by invalid task id") {
        taskOperations
            .selectTaskName(notExistingId)
            .shouldBeNull()
    }
})
