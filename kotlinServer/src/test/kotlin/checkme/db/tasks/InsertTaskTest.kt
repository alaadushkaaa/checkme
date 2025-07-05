package checkme.db.tasks

import checkme.db.TestcontainerSpec
import checkme.db.validTask
import checkme.domain.models.FormatOfAnswer
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class InsertTaskTest : TestcontainerSpec ({ context ->
    val taskOperations = TasksOperations(context)

    test("Valid task insertion should return this task") {
        val taskForInsert = validTask
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

    for (type in FormatOfAnswer.entries) {
        test("Valid task with answer type $type can be inserted") {
            val taskForInsert = validTask
            val insertedTask =
                taskOperations.insertTask(
                    taskForInsert.name,
                    taskForInsert.criterions,
                    type,
                    taskForInsert.description
                ).shouldNotBeNull()

            insertedTask.name.shouldBe(taskForInsert.name)
            insertedTask.criterions.shouldBe(taskForInsert.criterions)
            insertedTask.answerFormat.shouldBe(type)
            insertedTask.description.shouldBe(taskForInsert.description)
        }
    }
})
