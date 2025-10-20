package checkme.domain.operations.checks

import checkme.db.validChecksMany
import checkme.domain.models.Check
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

const val PAGE = 1
const val PAGECOUNT = 10

class FetchCheckTest : FunSpec({
    val checks = validChecksMany
    val check = validChecksMany[1]

    val fetchAllChecksMock: () -> List<Check> = { checks }
    val fetchCheckByIdMock: (Int) -> Check? = { id -> checks.firstOrNull { it.id == id } }
    val fetchCheckByIdNullMock: (Int) -> Check? = { null }
    val fetchChecksByUserIdMock: (Int) -> List<Check>? = { id -> checks.filter { it.userId == id } }
    val fetchChecksByUserIdNullMock: (Int) -> List<Check>? = { null }
    val fetchChecksByTaskIdMock: (Int) -> List<Check>? = { id -> checks.filter { it.taskId == id } }
    val fetchChecksByTaskIdNullMock: (Int) -> List<Check>? = { null }
    val fetchAllChecksPaginationMock: (Int) -> List<Check>? =
        { page ->
            if (checks.size > 9 + (page - 1) * 10) {
                checks.slice(0 + (page - 1) * 10..9 + (page - 1) * 10)
            } else {
                null
            }
        }

    val fetchAllChecks = FetchAllChecks(fetchAllChecksMock)
    val fetchCheckById = FetchCheckById(fetchCheckByIdMock)
    val fetchChecksByUserId = FetchChecksByUserId(fetchChecksByUserIdMock)
    val fetchChecksByUserIdNull = FetchChecksByUserId(fetchChecksByUserIdNullMock)
    val fetchChecksByTaskId = FetchChecksByTaskId(fetchChecksByTaskIdMock)
    val fetchChecksByTaskIdNull = FetchChecksByTaskId(fetchChecksByTaskIdNullMock)
    val fetchAllChecksPagination = FetchAllChecksPagination(fetchAllChecksPaginationMock)

    test("Fetch all checks should return list of checks and list have size more than one") {
        fetchAllChecks().shouldBeSuccess().shouldHaveSize(checks.size)
    }

    test("Fetch check by valid check id") {
        fetchCheckById(check.id).shouldBeSuccess() shouldBe check
    }

    test("Fetch check by id should return an error if id is not valid") {
        fetchCheckById(checks.maxOf { it.id } + 1).shouldBeFailure(CheckFetchingError.NO_SUCH_CHECK)
    }

    test("Fetch check by user id should return checks if user id is valid") {
        fetchChecksByUserId(check.userId).shouldBeSuccess()
            .shouldHaveSize(checks.filter { it.userId == check.userId }.size) shouldBe checks.filter { it.userId == check.userId }
    }

    test("Fetch check by user id should return an error if user id is not valid") {
        fetchChecksByUserIdNull(checks.maxOf { it.userId } + 1).shouldBeFailure(CheckFetchingError.NO_SUCH_CHECK)
    }

    test("Fetch check by task id should return checks if task id is valid") {
        fetchChecksByTaskId(check.taskId).shouldBeSuccess()
            .shouldHaveSize(checks.filter { it.taskId == check.taskId }.size)
    }

    test("Fetch check by task id should return return an error if check id is not valid") {
        fetchChecksByTaskIdNull(checks.maxOf { it.taskId } + 1).shouldBeFailure(CheckFetchingError.NO_SUCH_CHECK)
    }

    test("Fetch all check pagination should return checks if page is is valid") {
        fetchAllChecksPagination(PAGE).shouldBeSuccess()
            .shouldHaveSize(PAGE * PAGECOUNT)
    }

    test("Fetch all check pagination should return an error if page number is is not valid") {
        fetchAllChecksPagination(checks.size / 10 + 1).shouldBeFailure(CheckFetchingError.NO_SUCH_CHECK)
    }
})
