package checkme.config

import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.lens.nonBlankString
import org.http4k.lens.string

class AuthConfig (
    val salt: String,
    val secret: String,
    val seed: String,
    val generalPass: String,
) {
    companion object {
        fun fromEnvironment(environment: Environment) =
            AuthConfig(
                saltLens(environment),
                secretLens(environment),
                seedLens(environment),
                generalPasswordLens(environment),
            )

        private val saltLens = EnvironmentKey.string().required(
            "auth.salt",
            "Salt the application use to hash passwords",
        )

        private val secretLens = EnvironmentKey.string().required(
            "auth.secret",
            "Secret the application use",
        )

        private val seedLens = EnvironmentKey.string().required(
            "auth.seed",
            "Seed for password generation"
        )

        private val generalPasswordLens = EnvironmentKey.nonBlankString().required(
            "auth.generalPass",
            "Password for general admin",
        )
    }
}
