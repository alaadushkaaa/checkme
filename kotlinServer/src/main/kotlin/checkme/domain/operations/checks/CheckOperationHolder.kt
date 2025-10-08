package checkme.domain.operations.checks

import checkme.domain.forms.CheckResult
import checkme.domain.models.Check
import checkme.domain.operations.dependencies.checks.ChecksDatabase
import dev.forkhandles.result4k.Result
import java.time.LocalDateTime

class CheckOperationHolder (
    private val checksDatabase: ChecksDatabase,
) {
    val fetchCheckById: (Int) -> Result<Check, CheckFetchingError> =
        FetchCheckById {
                checkId: Int ->
            checksDatabase.selectCheckById(checkId)
        }

    val fetchAllChecksPagination: (Int) -> Result<List<Check>, CheckFetchingError> =
        FetchAllChecksPagination {
                page: Int ->
            checksDatabase.selectAllChecksPagination(page)
        }

    val fetchAllChecks: () -> Result<List<Check>, CheckFetchingError> =
        FetchAllChecks {
            checksDatabase.selectAllChecks()
        }

    val fetchChecksByUserId: (Int) -> Result<List<Check>, CheckFetchingError> =
        FetchChecksByUserId {
                userId: Int,
            ->
            checksDatabase.selectChecksByUserId(userId)
        }

    val fetchChecksByTaskId: (Int) -> Result<List<Check>, CheckFetchingError> =
        FetchChecksByTaskId {
                taskId: Int,
            ->
            checksDatabase.selectChecksByTaskId(taskId)
        }

    val createCheck: (
        taskId: Int,
        userId: Int,
        date: LocalDateTime,
        result: Map<String, CheckResult>?,
        status: String,
    ) -> Result<Check, CreateCheckError> =
        CreateCheck {
                taskId: Int,
                userId: Int,
                date: LocalDateTime,
                result: Map<String, CheckResult>?,
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
        status: String,
    ) -> Result<Check, ModifyCheckError> =
        ModifyCheckStatus {
                checkId: Int,
                status: String,
            ->
            checksDatabase.updateCheckStatus(checkId, status)
        }

    val updateCheckResult: (
        checkId: Int,
        result: Map<String, CheckResult>,
    ) -> Result<Check, ModifyCheckError> =
        ModifyCheckResult {
                checkId: Int,
                result: Map<String, CheckResult>,
            ->
            checksDatabase.updateCheckResult(checkId, result)
        }
}
