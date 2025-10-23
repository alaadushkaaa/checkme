package checkme.domain.operations.checks

import checkme.domain.operations.dependencies.checks.ChecksDatabase
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.mock

class CheckOperationHolderTest : FunSpec({
    val checksOperations: ChecksDatabase = mock()
    val checkOperationsHolder = CheckOperationHolder(checksOperations)

    test("CheckOperationsHolder should initialize with provided check operations") {
        checkOperationsHolder.fetchCheckById::class.shouldBe(FetchCheckById::class)
        checkOperationsHolder.fetchAllChecksPagination::class.shouldBe(FetchAllChecksPagination::class)
        checkOperationsHolder.fetchAllChecks::class.shouldBe(FetchAllChecks::class)
        checkOperationsHolder.fetchChecksByUserId::class.shouldBe(FetchChecksByUserId::class)
        checkOperationsHolder.fetchChecksByTaskId::class.shouldBe(FetchChecksByTaskId::class)
        checkOperationsHolder.createCheck::class.shouldBe(CreateCheck::class)
        checkOperationsHolder.updateCheckStatus::class.shouldBe(ModifyCheckStatus::class)
        checkOperationsHolder.updateCheckResult::class.shouldBe(ModifyCheckResult::class)
    }
})
