package checkme.domain.operations.checks

import checkme.domain.forms.CheckResult
import checkme.domain.models.Check
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import java.util.UUID

class ModifyCheckResult(
    private val updateCheckResult: (
        checkId: UUID,
        result: Map<String, CheckResult>,
    ) -> Check?,
) : (UUID, Map<String, CheckResult>) -> Result4k<Check, ModifyCheckError> {
    override fun invoke(
        checkId: UUID,
        result: Map<String, CheckResult>,
    ): Result4k<Check, ModifyCheckError> =
        when (
            val editedCheck = updateCheckResult(
                checkId,
                result
            )
        ) {
            is Check -> Success(editedCheck)
            else -> {
                Failure(ModifyCheckError.UNKNOWN_DATABASE_ERROR)
            }
        }
}

class ModifyCheckStatus(
    private val updateCheckStatus: (
        checkId: UUID,
        status: String,
    ) -> Check?,
) : (UUID, String) -> Result4k<Check, ModifyCheckError> {
    override fun invoke(
        checkId: UUID,
        status: String,
    ): Result4k<Check, ModifyCheckError> =
        when (
            val editedCheck = updateCheckStatus(
                checkId,
                status
            )
        ) {
            is Check -> Success(editedCheck)
            else -> {
                Failure(ModifyCheckError.UNKNOWN_DATABASE_ERROR)
            }
        }
}

enum class ModifyCheckError {
    UNKNOWN_DATABASE_ERROR,
}
