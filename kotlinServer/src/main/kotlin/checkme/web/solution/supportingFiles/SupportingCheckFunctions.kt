package checkme.web.solution.supportingFiles

import checkme.domain.forms.CheckResult
import checkme.domain.models.Check
import checkme.domain.operations.checks.CheckFetchingError
import checkme.domain.operations.checks.CheckOperationHolder
import checkme.domain.operations.checks.CreateCheckError
import checkme.domain.operations.tasks.TaskOperationsHolder
import checkme.domain.operations.users.ModifyCheckError
import checkme.web.solution.handlers.CreationCheckError
import checkme.web.solution.handlers.FetchingCheckError
import checkme.web.solution.handlers.ModifyingCheckError
import checkme.web.tasks.handlers.taskExists
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import org.http4k.core.*
import java.time.LocalDateTime

internal fun setStatusError(
    check: Check,
    checkOperations: CheckOperationHolder,
): Response {
    val objectMapper = jacksonObjectMapper()
    return when (
        val updatedCheckStatusError = updateCheckStatus(
            check.id,
            "Runtime error",
            checkOperations
        )
    ) {
        is Failure -> Response(Status.BAD_REQUEST)
            .body(
                objectMapper.writeValueAsString(
                    mapOf("error" to updatedCheckStatusError.reason.errorText)
                )
            )

        is Success -> Response(Status.OK).body(
            objectMapper.writeValueAsString(
                mapOf("checkId" to check.id)
            )
        )
    }
}

internal fun setStatusChecked(
    check: Check,
    checkOperations: CheckOperationHolder,
): Response {
    val objectMapper = jacksonObjectMapper()
    return when (
        val updatedCheckStatus = updateCheckStatus(
            check.id,
            "Проверено",
            checkOperations
        )
    ) {
        is Failure -> Response(Status.BAD_REQUEST)
            .body(
                objectMapper.writeValueAsString(
                    mapOf("error" to updatedCheckStatus.reason.errorText)
                )
            )

        is Success -> Response(Status.OK).body(
            objectMapper.writeValueAsString(
                mapOf("checkId" to check.id)
            )
        )
    }
}

internal fun createNewCheck(
    taskId: Int,
    userId: Int,
    checkOperations: CheckOperationHolder,
    taskOperations: TaskOperationsHolder,
): Result<Check, CreationCheckError> {
    if (taskExists(
            taskId = taskId,
            taskOperations = taskOperations
        )
    ) {
        return when (
            val newCheck = checkOperations.createCheck(
                taskId,
                userId,
                LocalDateTime.now(),
                null,
                "В процессе"
            )
        ) {
            is Failure -> when (newCheck.reason) {
                CreateCheckError.UNKNOWN_DATABASE_ERROR -> Failure(CreationCheckError.UNKNOWN_DATABASE_ERROR)
            }

            is Success -> Success(newCheck.value)
        }
    } else {
        return Failure(CreationCheckError.NO_TASK_FOR_CHECK_IN_DB)
    }
}

internal fun updateCheckResult(
    checkId: Int,
    checkResult: Map<String, CheckResult>,
    checkOperations: CheckOperationHolder,
): Result<Check, ModifyingCheckError> {
    return when (
        val updatedCheck = checkOperations.updateCheckResult(
            checkId,
            checkResult
        )
    ) {
        is Failure -> when (updatedCheck.reason) {
            ModifyCheckError.UNKNOWN_DATABASE_ERROR -> Failure(ModifyingCheckError.UNKNOWN_DATABASE_ERROR)
        }

        is Success -> Success(updatedCheck.value)
    }
}

internal fun updateCheckStatus(
    checkId: Int,
    checkStatus: String,
    checkOperations: CheckOperationHolder,
): Result<Check, ModifyingCheckError> {
    return when (
        val updatedCheck = checkOperations.updateCheckStatus(
            checkId,
            checkStatus
        )
    ) {
        is Failure -> when (updatedCheck.reason) {
            ModifyCheckError.UNKNOWN_DATABASE_ERROR -> Failure(ModifyingCheckError.UNKNOWN_DATABASE_ERROR)
        }

        is Success -> Success(updatedCheck.value)
    }
}

internal fun fetchCheckById(
    checkId: Int,
    checkOperations: CheckOperationHolder,
): Result<Check, FetchingCheckError> {
    return when (
        val fetchedCheck = checkOperations.fetchCheckById(checkId)
    ) {
        is Failure -> when (fetchedCheck.reason) {
            CheckFetchingError.NO_SUCH_CHECK -> Failure(FetchingCheckError.NO_CHECK_IN_DB)
            CheckFetchingError.UNKNOWN_DATABASE_ERROR -> Failure(FetchingCheckError.UNKNOWN_DATABASE_ERROR)
        }

        is Success -> Success(fetchedCheck.value)
    }
}
