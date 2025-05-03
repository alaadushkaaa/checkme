package checkme.domain.operations.checks

import checkme.config.AppConfig
import checkme.domain.models.Check
import checkme.domain.operations.dependencies.ChecksDatabase
import checkme.domain.operations.users.ModifyCheckError
import checkme.domain.operations.users.ModifyCheckResult
import checkme.domain.operations.users.ModifyCheckStatus
import checkme.web.solution.forms.CheckResult
import dev.forkhandles.result4k.Result
import java.time.LocalDateTime

class CheckOperationHolder (
    private val checksDatabase: ChecksDatabase,
    config: AppConfig
) {
    val fetchCheckById: (Int) -> Result<Check, CheckFetchingError> =
        FetchCheckById {
            checkId: Int,
                -> checksDatabase.selectCheckById(checkId)
        }

    val fetchAllChecks: () -> Result<List<Check>, CheckFetchingError> =
        FetchAllChecks {
            checksDatabase.selectAllChecks()
        }

    val fetchChecksByUserId: (Int) -> Result<List<Check>, CheckFetchingError> =
    FetchChecksByUserId {
        userId: Int,
        -> checksDatabase.selectChecksByUserId(userId)
    }

    val createCheck: (
        taskId: Int,
        userId: Int,
        date: LocalDateTime,
        result: Map<String, CheckResult>,
        status: String,
    ) -> Result<Check, CreateCheckError> =
        CreateCheck {
            taskId: Int,
            userId: Int,
            date: LocalDateTime,
            result: Map<String, CheckResult>,
            status: String,
        ->
            checksDatabase.insertCheck(
                taskId = taskId,
                userId = userId,
                date = date,
                result = result,
                status = status
            )
        }

    val updateCheckStatus: (
            checkId: Int,
            status: String
    ) -> Result<Check, ModifyCheckError> =
        ModifyCheckStatus {
            checkId: Int,
            status: String,
                -> checksDatabase.updateCheckStatus(checkId, status)
    }

    val updateCheckResult: (
        checkId: Int,
        result: Map<String, CheckResult>
    ) -> Result<Check, ModifyCheckError> =
        ModifyCheckResult {
                checkId: Int,
                result: Map<String, CheckResult>,
            -> checksDatabase.updateCheckResult(checkId, result)
        }
}