package checkme.config

import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.lens.int
import org.http4k.lens.nonBlankString

class CheckDatabaseConfig(
    val host: String,
    val port: Int,
    val user: String,
    val password: String,
    val urlDatabaseType: String,
) {
    val jdbc get() = "$urlDatabaseType$host:$port"

    companion object {
        private val hostLens = EnvironmentKey.nonBlankString().required(
            "check_db.host",
            "Server name to test the solution",
        )

        private val portLens = EnvironmentKey.int().defaulted(
            "check_db.port",
            3306,
            "Check database port",
        )

        private val userLens = EnvironmentKey.nonBlankString().required(
            "check_db.user",
            "Name of the user for connection",
        )

        private val passwordLens = EnvironmentKey.nonBlankString().required(
            "check_db.password",
            "Check database user password"
        )

        private val urlDatabaseLens = EnvironmentKey.nonBlankString().required(
            "check_db.url",
            "Check database url"
        )

        fun fromEnvironment(environment: Environment) =
            CheckDatabaseConfig(
                hostLens(environment),
                portLens(environment),
                userLens(environment),
                passwordLens(environment),
                urlDatabaseLens(environment),
            )
    }
}
