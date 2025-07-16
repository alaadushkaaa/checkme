package checkme.db.tasks

import checkme.db.TestcontainerSpec
import checkme.db.validTasks
import checkme.domain.models.AnswerType
import checkme.domain.models.FormatOfAnswer
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class InsertTaskTest : TestcontainerSpec ({ context ->
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

    test("Valid task insertion should return this task") {
        val taskForInsert = validTasks.first()
        val insertedTask =
            taskOperations.insertTask(
                taskForInsert.name,
                taskForInsert.criterions,
                taskForInsert.answerFormat,
                taskForInsert.description
            ).shouldNotBeNull()

        insertedTask.name.shouldBe(taskForInsert.name)
        insertedTask.criterions.shouldBe(taskForInsert.criterions)
        insertedTask.answerFormat.shouldBe(taskForInsert.answerFormat)
        insertedTask.description.shouldBe(taskForInsert.description)
    }

    for (type in AnswerType.entries) {
        test("Valid task with answer type $type can be inserted") {
            val taskForInsert = validTasks.first()
            val insertedTask =
                taskOperations.insertTask(
                    taskForInsert.name,
                    taskForInsert.criterions,
                    mapOf(type.code to type),
                    taskForInsert.description
                ).shouldNotBeNull()

            insertedTask.name.shouldBe(taskForInsert.name)
            insertedTask.criterions.shouldBe(taskForInsert.criterions)
            insertedTask.answerFormat.shouldBe(mapOf(type.code to type))
            insertedTask.description.shouldBe(taskForInsert.description)
        }
    }
})
