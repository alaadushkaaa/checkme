@file:Suppress("detekt:MayBeConst")

package checkme.db

import checkme.config.AppConfig
import checkme.domain.accounts.PasswordHasher
import checkme.domain.accounts.Role
import checkme.domain.checks.Criterion
import checkme.domain.forms.CheckResult
import checkme.domain.models.AnswerType
import checkme.domain.models.Bundle
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

val validRole = Role.STUDENT

val validAdminRole = Role.ADMIN

val validDate = LocalDateTime.parse("2025-05-06T11:48:54.613093")

val config = AppConfig.fromEnvironment()

val validResult = mapOf(
    "check1" to CheckResult(10, "Проверка пройдена"),
    "check2" to CheckResult(0, "Проверка не пройдена")
)

val validResultOne = mapOf(
    "check1" to CheckResult(10, "Проверка пройдена")
)

val validResultMany = mapOf(
    "check1" to CheckResult(10, "Проверка пройдена"),
    "check2" to CheckResult(0, "Проверка не пройдена"),
    "check3" to CheckResult(5, "Проверка пройдена частично"),
    "check4" to CheckResult(3, "Проверка не пройдена не полностью")
)

val validStatusCorrect = "Проверено"

val validStatusProcess = "В процессе"

val validStatusError = "Ошибка выполнения"

val validChecks: List<Check> = listOf(
    Check(validCheckId, validTaskId, validUserId, validDate, validResult, validStatusCorrect),
    Check(validCheckId + 1, validTaskId + 1, validUserId, validDate, validResult, validStatusCorrect),
    Check(validCheckId + 2, validTaskId, validUserId + 1, validDate, null, validStatusError),
    Check(validCheckId + 3, validTaskId + 1, validUserId, validDate, null, validStatusProcess)
)

val validChecksMany: List<Check> = listOf(
    Check(validCheckId, validTaskId, validUserId, validDate, validResult, validStatusCorrect),
    Check(validCheckId + 1, validTaskId + 1, validUserId, validDate, validResult, validStatusCorrect),
    Check(validCheckId + 2, validTaskId, validUserId + 1, validDate, null, validStatusError),
    Check(validCheckId + 3, validTaskId + 1, validUserId, validDate, null, validStatusProcess),
    Check(validCheckId + 4, validTaskId, validUserId, validDate, validResult, validStatusCorrect),
    Check(validCheckId + 5, validTaskId + 1, validUserId, validDate, validResult, validStatusCorrect),
    Check(validCheckId + 6, validTaskId, validUserId + 1, validDate, null, validStatusError),
    Check(validCheckId + 7, validTaskId, validUserId, validDate, null, validStatusProcess),
    Check(validCheckId + 8, validTaskId, validUserId, validDate, null, validStatusError),
    Check(validCheckId + 9, validTaskId + 2, validUserId, validDate, validResult, validStatusCorrect),
    Check(validCheckId + 10, validTaskId + 2, validUserId, validDate, validResultOne, validStatusCorrect),
    Check(validCheckId + 11, validTaskId, validUserId + 2, validDate, validResultMany, validStatusCorrect)
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
            "выводить строку \"Incorrect input\" в случае ошибки",
        true
    ),
    Task(
        2,
        "Вычитание чисел",
        validCriterions,
        mapOf("Введите текст" to AnswerType.TEXT),
        "Вам необходимо написать " +
            "программу, выполняющую вычитание двух чисел. На вход подаются два числа - a и b, " +
            "в качестве результата - результат вычитания этих чисел. Некорректный ввод необходимо обрабатывать и " +
            "выводить строку \"Incorrect input\" в случае ошибки",
        false
    ),
    Task(
        3,
        "Тестовое задание",
        validCriterions,
        mapOf(
            "Введите текст" to AnswerType.TEXT,
            "Прикрепите файл" to AnswerType.FILE
        ),
        "Текст тестового задания",
        true
    ),
)

val validPort = 3206
val validHost = "localhost"
val validUserName = "root"
val validPassword = "password"
val validUrlDatabase = "jdbc:mariadb://"
val validOverall = true


val validBundles : List<Bundle> = listOf(
    Bundle(1, "Контрольная №1", mapOf(
        1 to validTasks[1].id,
        2 to validTasks[2].id,
    ), true),
    Bundle(1, "Контрольная №2", mapOf(
        1 to validTasks[2].id,
        2 to validTasks[3].id,
    ), false),
    Bundle(1, "Контрольная №3", mapOf(
        1 to validTasks[1].id,
        2 to validTasks[3].id,
        3 to validTasks[2].id,
    ), true),
    Bundle(1, "Контрольная №4", mapOf(
        1 to validTasks[2].id,
        2 to validTasks[1].id,
        3 to validTasks[3].id,
    ), false),
)

val appConfig = AppConfig.fromEnvironment()
val appConfiguredPasswordHasher = PasswordHasher(appConfig.authConfig)
