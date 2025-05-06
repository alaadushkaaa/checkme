package checkme.db

import checkme.config.AppConfig
import checkme.domain.accounts.PasswordHasher
import checkme.domain.models.Check
import checkme.web.solution.forms.CheckResult
import java.time.LocalDateTime

@Suppress("MayBeConst")
val validName = "Иван"

@Suppress("MayBeConst")
val validSurname = "Иванов"

@Suppress("MayBeConst")
val validLogin = "iivanov"

@Suppress("MayBeConst")
val validAdminLogin = "admin"

@Suppress("MayBeConst")
val validPass = "password"

@Suppress("MayBeConst")
val validCheckId = 1

@Suppress("MayBeConst")
val validTaskId = 1

@Suppress("MayBeConst")
val validUserId = 1

@Suppress("MayBeConst")
val validDate = LocalDateTime.parse("2025-05-06T11:48:54.613093")

@Suppress("MayBeConst")
val validResult = mapOf(
    "check1" to CheckResult(10, "Проверка пройдена"),
    "check2" to CheckResult(0, "Проверка не пройдена")
)

@Suppress("MayBeConst")
val validStatusCorrect = "Проверено"

@Suppress("MayBeConst")
val validStatusProcess = "В процессе"

@Suppress("MayBeConst")
val validStatusError = "Ошибка выполнения"

val validChecks: List<Check> = listOf(
    Check(validCheckId, validTaskId, validUserId, validDate, validResult, validStatusCorrect),
    Check(validCheckId + 1, validTaskId + 1, validUserId, validDate, validResult, validStatusCorrect),
    Check(validCheckId + 2, validTaskId, validUserId + 1, validDate, null, validStatusError),
    Check(validCheckId + 3, validTaskId, validUserId, validDate, null, validStatusProcess)
)

val appConfig = AppConfig.fromEnvironment()
val appConfiguredPasswordHasher = PasswordHasher(appConfig.authConfig)
