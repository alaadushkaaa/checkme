package checkme.db.checks

import checkme.db.TestcontainerSpec
import checkme.db.validChecks
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class InsertCheckTest : TestcontainerSpec({ context ->
    val checkOperations = CheckOperations(context)

    test("Valid check insertions should return this check") {
        val checkForInsert = validChecks.first()
        val insertedCheck =
            checkOperations.insertCheck(
                checkForInsert.taskId,
                checkForInsert.userId,
                checkForInsert.date,
                checkForInsert.result,
                checkForInsert.status,
            ).shouldNotBeNull()

        insertedCheck.taskId.shouldBe(checkForInsert.taskId)
        insertedCheck.userId.shouldBe(checkForInsert.userId)
        insertedCheck.date.shouldBe(checkForInsert.date)
        insertedCheck.result.shouldBe(checkForInsert.result)
        insertedCheck.status.shouldBe(checkForInsert.status)
    }

    test("Valid check with empty result can be inserted") {
        val checkForInsert = validChecks.filter { it.result.isEmpty() }.first()
        val insertedCheck =
            checkOperations.insertCheck(
                checkForInsert.taskId,
                checkForInsert.userId,
                checkForInsert.date,
                checkForInsert.result,
                checkForInsert.status,
            ).shouldNotBeNull()

        insertedCheck.taskId.shouldBe(checkForInsert.taskId)
        insertedCheck.userId.shouldBe(checkForInsert.userId)
        insertedCheck.date.shouldBe(checkForInsert.date)
        insertedCheck.result.shouldBe(checkForInsert.result)
        insertedCheck.status.shouldBe(checkForInsert.status)
    }
})
