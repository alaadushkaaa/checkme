package checkme.db.checks

import checkme.db.TestcontainerSpec
import checkme.db.validChecks
import checkme.db.validResult
import checkme.db.validStatusCorrect
import checkme.domain.models.Check
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class UpdateCheckTest : TestcontainerSpec({ context ->
    val checkOperations = CheckOperations(context)

    lateinit var insertedChecks: List<Check>

    beforeEach {
        insertedChecks =
            validChecks.map {
                checkOperations.insertCheck(
                    it.taskId,
                    it.userId,
                    it.date,
                    it.result,
                    it.status
                ).shouldNotBeNull()
            }
    }

    test("Check result can be updated") {
        val insertedCheck = insertedChecks.first { it.result == null }
        checkOperations.updateCheckResult(insertedCheck.id, validResult).shouldNotBeNull()

        val updatedCheck = checkOperations.selectCheckById(insertedCheck.id).shouldNotBeNull()

        updatedCheck.id shouldBe insertedCheck.id
        updatedCheck.userId shouldBe insertedCheck.userId
        updatedCheck.taskId shouldBe insertedCheck.taskId
        updatedCheck.date shouldBe insertedCheck.date
        updatedCheck.result shouldBe validResult
        updatedCheck.status shouldBe insertedCheck.status
    }

    test("Check status can be updated") {
        val insertedCheck = insertedChecks.first { it.status != validStatusCorrect }
        checkOperations.updateCheckStatus(insertedCheck.id, validStatusCorrect).shouldNotBeNull()

        val updatedCheck = checkOperations.selectCheckById(insertedCheck.id).shouldNotBeNull()

        updatedCheck.id shouldBe insertedCheck.id
        updatedCheck.userId shouldBe insertedCheck.userId
        updatedCheck.taskId shouldBe insertedCheck.taskId
        updatedCheck.date shouldBe insertedCheck.date
        updatedCheck.result shouldBe insertedCheck.result
        updatedCheck.status shouldBe validStatusCorrect
    }
})
