package checkme.domain.operations.dependencies.checks

import checkme.domain.forms.CheckResult
import checkme.domain.models.Check
import java.time.LocalDateTime

interface ChecksDatabase {
    fun selectCheckById(checkId: Int): Check?

    fun selectChecksByUserId(userId: Int): List<Check>

    fun selectChecksByTaskId(taskId: Int): List<Check>

    fun selectAllChecks(): List<Check>

    fun selectAllChecksPagination(page: Int): List<Check>

    fun insertCheck(
        taskId: Int,
        userId: Int,
        date: LocalDateTime,
        result: Map<String, CheckResult>?,
        status: String,
    ): Check?

    fun updateCheckStatus(
        checkId: Int,
        status: String,
    ): Check?

    fun updateCheckResult(
        checkId: Int,
        result: Map<String, CheckResult>?,
    ): Check?
}
