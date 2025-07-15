package checkme.db.tasks

import checkme.db.TestcontainerSpec
import checkme.db.validTasks
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class InsertTaskTest : TestcontainerSpec ({ context ->
    val taskOperations = TasksOperations(context)

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
})
