package checkme.config

import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.lens.boolean

class LoggingConfig (
    val overall: Boolean,
) {
    companion object {
        fun fromEnvironment(environment: Environment) =
            LoggingConfig(
                overallLens(environment),
            )

        private val overallLens = EnvironmentKey.boolean().required(
            "logging.overall",
            "Overall illustrate what kind of messages will be written by logger",
        )
    }
}
