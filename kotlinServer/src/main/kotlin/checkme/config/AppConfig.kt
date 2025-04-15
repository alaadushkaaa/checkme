package checkme.config

import org.http4k.cloudnative.env.Environment
import java.io.File

data class AppConfig(
    val databaseConfig: DatabaseConfig,
    val testsConfig: TestsConfig,
    val webConfig: WebConfig,
    val authConfig: AuthConfig,
) {
    companion object {
        private const val CONFIG = "app.properties"

        val APP_ENV = Environment.ENV overrides
            fromProperties() overrides
            Environment.JVM_PROPERTIES

        fun fromEnvironment(environment: Environment = APP_ENV) =
            AppConfig(
                DatabaseConfig.fromEnvironment(environment),
                TestsConfig.fromEnvironment(environment),
                WebConfig.fromEnvironment(environment),
                AuthConfig.fromEnvironment(environment)
            )

        private fun fromProperties() =
            File(CONFIG).let { configFile ->
                when {
                    configFile.exists() -> Environment.from(configFile)
                    else -> Environment.EMPTY
                }
            }
    }
}
