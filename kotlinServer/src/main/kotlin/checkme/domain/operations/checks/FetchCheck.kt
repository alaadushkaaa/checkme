package checkme.domain.operations.checks

import checkme.domain.models.Check
import checkme.web.solution.forms.CheckWithAllData
import checkme.web.solution.forms.CheckWithTaskData
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import org.jooq.exception.DataAccessException

class FetchCheckById(
    private val fetchCheckById: (Int) -> Check?,
) : (Int) -> Result4k<Check, CheckFetchingError> {

    override fun invoke(checkId: Int): Result4k<Check, CheckFetchingError> =
        try {
            when (val check = fetchCheckById(checkId)) {
                is Check -> Success(check)
                else -> Failure(CheckFetchingError.NO_SUCH_CHECK)
            }
        } catch (_: DataAccessException) {
            Failure(CheckFetchingError.UNKNOWN_DATABASE_ERROR)
        }
}

class FetchCheckDataById(
    private val fetchCheckDataById: (Int) -> CheckWithAllData?,
) : (Int) -> Result4k<CheckWithAllData, CheckFetchingError> {
    override fun invoke(checkId: Int): Result4k<CheckWithAllData, CheckFetchingError> =
        try {
            when (val checkWithData = fetchCheckDataById(checkId)) {
                is CheckWithAllData -> Success(checkWithData)
                else -> Failure(CheckFetchingError.NO_SUCH_CHECK)
            }
        } catch (_: DataAccessException) {
            Failure(CheckFetchingError.UNKNOWN_DATABASE_ERROR)
        }
}

class FetchChecksByUserId(
    private val fetchChecksByUserId: (Int) -> List<Check>?,
) : (Int) -> Result4k<List<Check>, CheckFetchingError> {

    override fun invoke(userId: Int): Result4k<List<Check>, CheckFetchingError> =
        try {
            when (val checks = fetchChecksByUserId(userId)) {
                is List<Check> -> Success(checks)
                else -> Failure(CheckFetchingError.NO_SUCH_CHECK)
            }
        } catch (_: DataAccessException) {
            Failure(CheckFetchingError.UNKNOWN_DATABASE_ERROR)
        }
}

class FetchAllChecksWithData(
    private val fetchAllChecksWithData: (Int?) -> List<CheckWithAllData>?,
) : (Int?) -> Result4k<List<CheckWithAllData>, CheckFetchingError> {

    override fun invoke(page: Int?): Result4k<List<CheckWithAllData>, CheckFetchingError> =
        try {
            when (val checks = fetchAllChecksWithData(page)) {
                is List<CheckWithAllData> -> Success(checks)
                else -> Failure(CheckFetchingError.NO_SUCH_CHECK)
            }
        } catch (_: DataAccessException) {
            Failure(CheckFetchingError.UNKNOWN_DATABASE_ERROR)
        }
}

class FetchAllUsersChecksWithTaskData(
    private val fetchAllUsersChecksWithTaskData: (Int) -> List<CheckWithTaskData>?,
) : (Int) -> Result4k<List<CheckWithTaskData>, CheckFetchingError> {

    override fun invoke(userId: Int): Result4k<List<CheckWithTaskData>, CheckFetchingError> =
        try {
            when (val checks = fetchAllUsersChecksWithTaskData(userId)) {
                is List<CheckWithTaskData> -> Success(checks)
                else -> Failure(CheckFetchingError.NO_SUCH_CHECK)
            }
        } catch (_: DataAccessException) {
            Failure(CheckFetchingError.UNKNOWN_DATABASE_ERROR)
        }
}

class FetchAllChecks(
    private val fetchAllChecks: () -> List<Check>?,
) : () -> Result4k<List<Check>, CheckFetchingError> {

    override fun invoke(): Result4k<List<Check>, CheckFetchingError> =
        try {
            when (val checks = fetchAllChecks()) {
                is List<Check> -> Success(checks)
                else -> Failure(CheckFetchingError.NO_SUCH_CHECK)
            }
        } catch (_: DataAccessException) {
            Failure(CheckFetchingError.UNKNOWN_DATABASE_ERROR)
        }
}

enum class CheckFetchingError {
    UNKNOWN_DATABASE_ERROR,
    NO_SUCH_CHECK,
}
