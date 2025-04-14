package checkme.db

import checkme.config.AppConfig
import checkme.domain.accounts.PasswordHasher

@Suppress("MayBeConst")
val validName = "Иван"

@Suppress("MayBeConst")
val validSurname = "Иванов"

@Suppress("MayBeConst")
val validPass = "password"

val appConfig = AppConfig.fromEnvironment()
val appConfiguredPasswordHasher = PasswordHasher(appConfig.authConfig)
