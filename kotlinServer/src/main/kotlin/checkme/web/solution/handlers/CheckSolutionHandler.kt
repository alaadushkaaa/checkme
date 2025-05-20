package checkme.web.solution.handlers

import checkme.domain.models.Task
import checkme.domain.operations.checks.CheckOperationHolder
import checkme.web.solution.checks.Criterion
import checkme.web.solution.forms.CheckSolutionRequest
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.*

const val COMPLETE_TASK = 10
const val SOLUTIONS_DIR = "/solutions"
const val SOLUTION_DIR = "/solution"

class CheckSolutionHandler(
    private val checkOperations: CheckOperationHolder,
) : HttpHandler {
    @Suppress("LongMethod", "NestedBlockDepth")
    override fun invoke(request: Request): Response {
//        beforeAll.py - выполняется перед всеми тестами.
//        beforeEach.py - выполняется перед каждым тестом.
//        afterEach.py - выполняется после каждого теста.
//        afterAll.py - выполняется после всех тестов.

        val objectMapper = jacksonObjectMapper()
        val checkSolutionRequest = objectMapper.readValue<CheckSolutionRequest>(request.bodyString())
        val taskId = checkSolutionRequest.taskId
        val form = checkSolutionRequest.form

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

        return when (val newCheck = createNewCheck(taskId.toInt(), checkSolutionRequest.authUser.id, checkOperations)) {
            is Failure -> Response(Status.INTERNAL_SERVER_ERROR)
                .body(objectMapper.writeValueAsString(mapOf("error" to newCheck.reason.errorText)))

            is Success -> {
                val answers = mutableListOf<String>()
                var index = 0
                for (field in form.fields) {
                    if (field.key == index.toString()) {
                        answers.add(field.value.toString())
                        index++
                    } else {
                        if (form.files.containsKey(index.toString())) {
                            val file = form.files[index.toString()]?.first()
                            if (file != null) {
                                val pathToFile = tryAddFileToUserSolutionDirectory(
                                    checkId = newCheck.value.id,
                                    user = checkSolutionRequest.authUser,
                                    file
                                )
                                answers.add(pathToFile)
                            }
                            index++
                        }
                    }
                }
                val checksResult = checkStudentAnswer(task, newCheck.value.id)
                if (checksResult == null) {
                    setStatusError(newCheck.value, checkOperations)
                } else {
                    when (val updatedCheck = updateCheckResult(newCheck.value.id, checksResult, checkOperations)) {
                        is Failure -> Response(Status.INTERNAL_SERVER_ERROR)
                            .body(
                                objectMapper.writeValueAsString(
                                    mapOf("error" to updatedCheck.reason.errorText)
                                )
                            )

                        is Success -> setStatusChecked(updatedCheck.value, checkOperations)
                    }
                }
            }
        }
    }
}

enum class CreationCheckError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Что-то случилось. Пожалуйста, повторите попытку позднее или обратитесь за помощью"),
}

enum class ModifyingCheckError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Что-то случилось. Пожалуйста, повторите попытку позднее или обратитесь за помощью"),
}
