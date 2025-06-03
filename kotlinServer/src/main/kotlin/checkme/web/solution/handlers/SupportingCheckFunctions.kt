package checkme.web.solution.handlers

import checkme.domain.models.Check
import checkme.domain.models.CheckType
import checkme.domain.models.Task
import checkme.domain.models.User
import checkme.domain.operations.checks.CheckOperationHolder
import checkme.domain.operations.checks.CreateCheckError
import checkme.domain.operations.users.ModifyCheckError
import checkme.web.solution.checks.CheckDataConsole
import checkme.web.solution.checks.Criterion
import checkme.web.solution.forms.CheckResult
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import org.http4k.core.*
import org.http4k.lens.MultipartFormFile
import java.io.File
import java.time.LocalDateTime

internal fun setStatusError(
    check: Check,
    checkOperations: CheckOperationHolder,
): Response {
    val objectMapper = jacksonObjectMapper()
    return when (
        val updatedCheckStatusError = updateCheckStatus(
            check.id,
            "Ошибка выполнения",
            checkOperations
        )
    ) {
        is Failure -> Response(Status.INTERNAL_SERVER_ERROR)
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
        is Failure -> Response(Status.INTERNAL_SERVER_ERROR)
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
): Result<Check, CreationCheckError> {
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

internal fun checkStudentAnswer(
    task: Task,
    checkId: Int,
): Map<String, CheckResult>? {
    val results = mutableMapOf<String, CheckResult>()
    val specialCriterions = listOf("beforeAll.json", " beforeEach.json", "afterEach.json", "afterAll.json")

    val beforeAllCriterion = task.criterions.entries.firstOrNull {it.value.test == "beforeAll.json"}
    val beforeAllResult = beforeAllCriterion
        ?.let { criterionCheck(it, task, checkId) }
        ?: return null
    results[beforeAllCriterion.key] = beforeAllResult

    for (criterion in task.criterions) {
        val beforeEachCriterion = task.criterions.entries.firstOrNull {it.value.test == "beforeEach.json"}
        if (beforeEachCriterion != null) {
            if (results[beforeEachCriterion.key] != null && results[beforeEachCriterion.key]?.score != 0) {
                val beforeEachResult = criterionCheck(beforeEachCriterion, task, checkId)
                    ?: return null
                results[beforeEachCriterion.key] = beforeEachResult
            }
        }
        if (!specialCriterions.contains(criterion.value.test)) {
            val checkResult = criterionCheck(criterion, task, checkId) ?: return null
            results[criterion.key] = checkResult
        }
        val afterEachCriterion = task.criterions.entries.firstOrNull {it.value.test == "afterEach.json"}
        if (afterEachCriterion != null) {
            if (results[afterEachCriterion.key] != null && results[afterEachCriterion.key]?.score != 0) {
                val afterEachResult = criterionCheck(afterEachCriterion, task, checkId)
                    ?: return null
                results[afterEachCriterion.key] = afterEachResult
            }
        }
    }

    val afterAllCriterion = task.criterions.entries.firstOrNull {it.value.test == "afterAll.json"}
    val afterAllResult = afterAllCriterion
        ?.let { criterionCheck(it, task, checkId) }
        ?: return null
    results[afterAllCriterion.key] = afterAllResult
    return results
}

private fun criterionCheck(
    criterion: Map.Entry<String, Criterion>,
    task: Task,
    checkId: Int
) : CheckResult? {
    val objectMapper = jacksonObjectMapper()
    val checkFile = findCheckFile("/src/main/resources/tasks/task${task.id}", criterion.key)
    val jsonString = checkFile?.readText()
    if (jsonString == null) {
        return null
    } else {
        val jsonWithCheck = objectMapper.readTree(jsonString)
        val type = jsonWithCheck.get("type")?.asText()
        println("Type: $type")
        when (CheckType.valueOf(type.toString())) {
            CheckType.CONSOLE_CHECK -> {
                val check = objectMapper.readValue<CheckDataConsole>(jsonString)
                    return CheckDataConsole.consoleCheck(task, check, checkId, criterion.value)
            }
        }
    }
}

private fun findCheckFile(
    directoryPath: String,
    fileName: String,
): File? {
    val dir = File(directoryPath)
    if (!dir.isDirectory) return null
    return dir.listFiles()?.firstOrNull { it.name == fileName }
}

internal fun tryAddFileToUserSolutionDirectory(
    checkId: Int,
    user: User,
    file: MultipartFormFile,
): String {
    println("Я сюда попал")
    val solutionDir = File(
        "..$SOLUTIONS_DIR$SOLUTION_DIR$checkId" +
            "-${user.name}" +
            "-${user.surname}"
    )
    if (!solutionDir.exists()) {
        solutionDir.mkdirs()
    }
    val fileName = file.filename
    val filePath = File(solutionDir, fileName)
    file.content.use { input ->
        filePath.outputStream().use { output ->
            input.copyTo(output)
        }
    }
    return "$filePath/${file.filename}"
}
