package checkme.web.solution.handlers

import checkme.domain.models.Check
import checkme.domain.models.CheckType
import checkme.domain.models.Task
import checkme.domain.operations.checks.CheckOperationHolder
import checkme.domain.operations.checks.CreateCheckError
import checkme.domain.operations.users.ModifyCheckError
import checkme.web.solution.checks.CheckDataConsole
import checkme.web.solution.checks.Criterion
import checkme.web.solution.forms.CheckResult
import checkme.web.solution.forms.CheckSolutionRequest
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import org.http4k.core.*
import java.io.File
import java.time.LocalDateTime

class CheckSolutionHandler(
    private val checkOperations: CheckOperationHolder,
) : HttpHandler {
    @Suppress("LongMethod")
    override fun invoke(request: Request): Response {
//        beforeAll.py - выполняется перед всеми тестами.
//        beforeEach.py - выполняется перед каждым тестом.
//        afterEach.py - выполняется после каждого теста.
//        afterAll.py - выполняется после всех тестов.

        val objectMapper = jacksonObjectMapper()
        val checkSolutionRequest = objectMapper.readValue<CheckSolutionRequest>(request.bodyString())
        val taskId = checkSolutionRequest.taskId
        val form = checkSolutionRequest.form

        val answers = mutableListOf<String>()
        var index = 0
        // придется подменить, пока нет базы заданий
        for (field in form.fields) {
            if (field.key == index.toString()) {
                answers.add(field.value.toString())
                index++
            } else {
                if (form.files.containsKey(index.toString())) {
                    val file = form.files[index.toString()]
//                    val filePath = "uploads/file-${index}"
//                    File(filePath).writeBytes(file)

                    answers.add(file.toString())
                    index++
                }
            }
        }

        // todo получение задания из бд
        // todo для каждого задания создается папка - внутри нее файлы-проверки, относящиеся к заданию
        val criterions = mapOf(
            "Сложение положительных чисел" to
                Criterion(
                    "Сложение чисел происходит корреткно",
                    COMPLETE_TASK,
                    "plus_numbers.json",
                    "Числа складываются неправильно"
                ),
            "Некорректный ввод" to
                Criterion(
                    "Случай некоректного ввода обрабатывается",
                    COMPLETE_TASK,
                    "incorrect_input.json",
                    "Не обработан случай некорректного ввода чисел"
                )
        )
        val task = Task(
            1,
            "Суммирование чисел",
            criterions,
            "Файл",
            "Вам необходимо написать " +
                "программу, выполняющую суммирование двух чисел. На вход подаются два числа - a и b, " +
                "в качестве результата - сумма этих чисел. Некорректный ввод необходимо обрабатыввать и " +
                "выводить строку \"Incorrect input\" в случае ошибки"
        )

        return when (val newCheck = createNewCheck(taskId.toInt(), checkSolutionRequest.authUser.id)) {
            is Failure -> Response(Status.INTERNAL_SERVER_ERROR)
                .body(objectMapper.writeValueAsString(mapOf("error" to newCheck.reason.errorText)))

            is Success -> {
                val checksResult = checkStudentAnswer(task, newCheck.value.id)
                if (checksResult == null) {
                    setStatusError(newCheck.value)
                } else {
                    when (val updatedCheck = updateCheckResult(newCheck.value.id, checksResult)) {
                        is Failure -> Response(Status.INTERNAL_SERVER_ERROR)
                            .body(
                                objectMapper.writeValueAsString(
                                    mapOf("error" to updatedCheck.reason.errorText)
                                )
                            )

                        is Success -> setStatusChecked(updatedCheck.value)
                    }
                }
            }
        }
    }

    private fun setStatusError(check: Check): Response {
        val objectMapper = jacksonObjectMapper()
        // todo посмотреть какой ответ уходит при ошибке выполнения!!
        return when (
            val updatedCheckStatusError = updateCheckStatus(
                check.id,
                "Ошибка выполнения"
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

    private fun setStatusChecked(check: Check): Response {
        val objectMapper = jacksonObjectMapper()
        return when (
            val updatedCheckStatus = updateCheckStatus(
                check.id,
                "Проверено"
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

    private fun createNewCheck(
        taskId: Int,
        userId: Int,
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

    private fun updateCheckResult(
        checkId: Int,
        checkResult: Map<String, CheckResult>,
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

    private fun updateCheckStatus(
        checkId: Int,
        checkStatus: String,
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
}

private fun checkStudentAnswer(
    task: Task,
    checkId: Int,
): Map<String, CheckResult>? {
    val results = mutableMapOf<String, CheckResult>()
    val objectMapper = jacksonObjectMapper()
    for (criterion in task.criterions) {
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
                    val checkResult =
                        CheckDataConsole.consoleCheck(task, check, checkId, criterion.value)
                    results[criterion.key] = checkResult
                }
            }
        }
    }
    return results
}

private fun findCheckFile(
    directoryPath: String,
    fileName: String,
): File? {
    val dir = File(directoryPath)
    if (!dir.isDirectory) return null
    return dir.listFiles()?.firstOrNull { it.name == fileName }
}

enum class CreationCheckError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Что-то случилось. Пожалуйста, повторите попытку позднее или обратитесь за помощью"),
}

enum class ModifyingCheckError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Что-то случилось. Пожалуйста, повторите попытку позднее или обратитесь за помощью"),
}

const val COMPLETE_TASK = 10
const val NOT_COMPLETE_TASK = 0
