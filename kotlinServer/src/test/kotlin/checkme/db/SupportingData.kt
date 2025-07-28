@file:Suppress("detekt:MayBeConst")

package checkme.db

import checkme.config.AppConfig
import checkme.domain.accounts.PasswordHasher
import checkme.domain.checks.Criterion
import checkme.domain.forms.CheckResult
import checkme.domain.models.AnswerType
import checkme.domain.models.Check
import checkme.domain.models.Task
import checkme.web.solution.handlers.COMPLETE_TASK
import java.time.LocalDateTime

val validName = "Иван"

val validSurname = "Иванов"

val validLogin = "iivanov"

val validAdminLogin = "admin"

val validPass = "password"

val validCheckId = 1

val validTaskId = 1

val validUserId = 1

val validDate = LocalDateTime.parse("2025-05-06T11:48:54.613093")

val validResult = mapOf(
    "check1" to CheckResult(10, "Проверка пройдена"),
    "check2" to CheckResult(0, "Проверка не пройдена")
)

val validStatusCorrect = "Проверено"

val validStatusProcess = "В процессе"

val validStatusError = "Ошибка выполнения"

val validChecks: List<Check> = listOf(
    Check(validCheckId, validTaskId, validUserId, validDate, validResult, validStatusCorrect),
    Check(validCheckId + 1, validTaskId + 1, validUserId, validDate, validResult, validStatusCorrect),
    Check(validCheckId + 2, validTaskId, validUserId + 1, validDate, null, validStatusError),
    Check(validCheckId + 3, validTaskId, validUserId, validDate, null, validStatusProcess)
)

val validCriterions = mapOf(
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

val validTasks: List<Task> = listOf(
    Task(
        1,
        "Суммирование чисел",
        validCriterions,
        mapOf("Прикрепите файл" to AnswerType.FILE),
        "Вам необходимо написать " +
            "программу, выполняющую суммирование двух чисел. На вход подаются два числа - a и b, " +
            "в качестве результата - сумма этих чисел. Некорректный ввод необходимо обрабатыввать и " +
            "выводить строку \"Incorrect input\" в случае ошибки"
    ),
    Task(
        2,
        "Вычитание чисел",
        validCriterions,
        mapOf("Введите текст" to AnswerType.TEXT),
        "Вам необходимо написать " +
            "программу, выполняющую вычитание двух чисел. На вход подаются два числа - a и b, " +
            "в качестве результата - результат вычитания этих чисел. Некорректный ввод необходимо обрабатывать и " +
            "выводить строку \"Incorrect input\" в случае ошибки"
    ),
    Task(
        3,
        "Тестовое задание",
        validCriterions,
        mapOf(
            "Введите текст" to AnswerType.TEXT,
            "Прикрепите файл" to AnswerType.FILE
        ),
        "Текст тестового задания"
    ),
)

val appConfig = AppConfig.fromEnvironment()
val appConfiguredPasswordHasher = PasswordHasher(appConfig.authConfig)
