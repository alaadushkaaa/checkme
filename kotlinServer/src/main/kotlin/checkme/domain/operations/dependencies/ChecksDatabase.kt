package checkme.domain.operations.dependencies

import checkme.domain.forms.CheckResult
import checkme.domain.models.Check
import checkme.web.solution.forms.CheckWithAllData
import checkme.web.solution.forms.CheckWithTaskData
import java.time.LocalDateTime

interface ChecksDatabase {
    fun selectCheckById(checkId: Int): Check?

    fun selectChecksByUserId(userId: Int): List<Check>

    fun selectAllChecks(): List<Check>

    fun selectAllUsersChecks(userId: Int): List<CheckWithTaskData>

    fun selectCheckByIdWithData(checkId: Int): CheckWithAllData?

    fun selectAllChecksWithData(page: Int?): List<CheckWithAllData>

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
