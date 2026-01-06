package checkme.domain.operations.checks

import checkme.db.validChecksMany
import checkme.domain.forms.CheckResult
import checkme.domain.models.Check
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.util.UUID

class ModifyCheckTest : FunSpec({
    isolationMode = IsolationMode.InstancePerTest

    val validChecks = validChecksMany
    val checkWithResult = validChecks.firstOrNull { it.result != validChecks.first().result && it.result != null }
    val checkWithStatus = validChecks.firstOrNull { it.status != validChecks.first().status }

    val updateCheckResultMock: (UUID, Map<String, CheckResult>) -> Check? =
        { id, result -> validChecks.find { it.id == id }?.copy(result = result) }
    val updateCheckResultNullMock: (UUID, Map<String, CheckResult>) -> Check? = { _, _ -> null }
    val updateCheckStatusMock: (UUID, String) -> Check? =
        { id, status -> validChecks.find { it.id == id }?.copy(status = status) }
    val updateCheckStatusNullMock: (UUID, String) -> Check? = { _, _ -> null }

    val modifyCheckResult = ModifyCheckResult(updateCheckResultMock)
    val modifyCheckResultNull = ModifyCheckResult(updateCheckResultNullMock)
    val modifyCheckStatus = ModifyCheckStatus(updateCheckStatusMock)
    val modifyCheckStatusNull = ModifyCheckStatus(updateCheckStatusNullMock)

    test("Check result can be updated if check exists") {
        modifyCheckResult(
            validChecks.first().id,
            checkWithResult!!.result!!
        ).shouldBeSuccess() shouldBe validChecks.first()
            .copy(result = checkWithResult.result)
    }

    test("Check result should return unknown database error if check doesn't exists") {
        modifyCheckResultNull(
            validChecks.first().id,
            checkWithResult!!.result!!
        ).shouldBeFailure(ModifyCheckError.UNKNOWN_DATABASE_ERROR)
    }

    test("Check status can be updated if check exists") {
        modifyCheckStatus(
            validChecks.first().id,
            checkWithStatus!!.status
        ).shouldBeSuccess() shouldBe validChecks.first()
            .copy(status = checkWithStatus.status)
    }

    test("Check status should return unknown database error if check doesn't exists") {
        modifyCheckStatusNull(
            validChecks.first().id,
            checkWithStatus!!.status
        ).shouldBeFailure(ModifyCheckError.UNKNOWN_DATABASE_ERROR)
    }
})
