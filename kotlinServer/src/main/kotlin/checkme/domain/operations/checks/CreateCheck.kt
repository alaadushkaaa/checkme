package checkme.domain.operations.checks

import checkme.domain.forms.CheckResult
import checkme.domain.models.Check
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import java.time.LocalDateTime
import java.util.UUID

class CreateCheck(
    private val insertCheck: (
        taskId: UUID,
        userId: UUID,
        date: LocalDateTime,
        result: Map<String, CheckResult>,
        status: String,
    ) -> Check?,
) : (
        UUID,
        UUID,
        LocalDateTime,
        Map<String, CheckResult>,
        String,
    ) -> Result4k<Check, CreateCheckError> {
    override fun invoke(
        taskId: UUID,
        userId: UUID,
        date: LocalDateTime,
        result: Map<String, CheckResult>,
        status: String,
    ): Result4k<Check, CreateCheckError> =
        when (
            val newCheck = insertCheck(
                taskId,
                userId,
                date,
                result,
                status
            )
        ) {
            is Check -> Success(newCheck)
            else -> {
                Failure(CreateCheckError.UNKNOWN_DATABASE_ERROR)
            }
        }
}

enum class CreateCheckError {
    UNKNOWN_DATABASE_ERROR,
}
