package checkme.db.checks

import checkme.db.TestcontainerSpec
import checkme.db.validChecks
import checkme.db.validChecksMany
import checkme.domain.models.Check
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class SelectCheckTest : TestcontainerSpec({ context ->
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

    test("Select check by id should return this check") {
        val selectedCheck = checkOperations.selectCheckById(insertedChecks.first().id).shouldNotBeNull()

        selectedCheck.id.shouldBe(insertedChecks.first().id)
        selectedCheck.taskId.shouldBe(insertedChecks.first().taskId)
        selectedCheck.userId.shouldBe(insertedChecks.first().userId)
        selectedCheck.date.shouldBe(insertedChecks.first().date)
        selectedCheck.result.shouldBe(insertedChecks.first().result)
        selectedCheck.status.shouldBe(insertedChecks.first().status)
    }

    test("Select check by invalid id should return null") {
        checkOperations.selectCheckById(insertedChecks.maxOf { it.id } + 1).shouldBeNull()
    }

    test("Select checks by valid userId should return this checks") {
        val selectedChecks = checkOperations.selectChecksByUserId(insertedChecks.first().userId).shouldNotBeNull()
        selectedChecks shouldContainExactlyInAnyOrder insertedChecks
            .filter { it.userId == insertedChecks.first().userId }
    }

    test("Select checks by invalid userId should return empty list") {
        checkOperations.selectChecksByUserId(insertedChecks.maxOf { it.userId } + 1)
            .shouldBeEmpty()
    }

    test("Select checks by valid taskId should return this checks") {
        val selectedChecks = checkOperations.selectChecksByTaskId(insertedChecks.first().taskId).shouldNotBeNull()
        selectedChecks shouldContainExactlyInAnyOrder insertedChecks
            .filter { it.taskId == insertedChecks.first().taskId }
    }

    test("Select checks by invalid taskId should return empty list") {
        checkOperations.selectChecksByTaskId(insertedChecks.maxOf { it.taskId } + 1)
            .shouldBeEmpty()
    }

    test("Select all checks should return all of this inserted checks") {
        val selectedChecks = checkOperations.selectAllChecks().shouldNotBeNull()
        selectedChecks shouldContainExactlyInAnyOrder insertedChecks
    }

    test(
        "Select all checks pagination should return a list with the size of the inserted checks if " +
            "there are less than 10"
    ) {
        val selectedChecksBefore = checkOperations.selectAllChecksPagination(1).shouldNotBeNull()
        selectedChecksBefore.size.shouldBe(insertedChecks.size)
    }

    test("Select all checks pagination should return a list with the size 10 if this count exists") {
        validChecksMany.map {
            checkOperations.insertCheck(
                it.taskId,
                it.userId,
                it.date,
                it.result,
                it.status
            ).shouldNotBeNull()
        }
        val selectedChecksBefore = checkOperations.selectAllChecksPagination(1).shouldNotBeNull()
        selectedChecksBefore.size.shouldBe(10)
    }
})
