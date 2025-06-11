@file:Suppress("detekt:MayBeConst")

package checkme.db

import checkme.config.AppConfig
import checkme.domain.accounts.PasswordHasher
import checkme.domain.forms.CheckResult
import checkme.domain.models.Check
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

val appConfig = AppConfig.fromEnvironment()
val appConfiguredPasswordHasher = PasswordHasher(appConfig.authConfig)
