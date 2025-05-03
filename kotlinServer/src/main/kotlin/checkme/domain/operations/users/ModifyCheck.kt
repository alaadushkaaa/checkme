package checkme.domain.operations.users

import checkme.domain.models.Check
import checkme.web.solution.forms.CheckResult
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success

class ModifyCheckResult(
    private val updateCheckResult: (
        checkId: Int,
        result: Map<String, CheckResult>,
    ) -> Check?,
) : (Int, Map<String, CheckResult>) -> Result4k<Check, ModifyCheckError> {
    override fun invoke(
        checkId: Int,
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
        checkId: Int,
        status: String,
    ) -> Check?,
) : (Int, String) -> Result4k<Check, ModifyCheckError> {
    override fun invoke(
        checkId: Int,
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
