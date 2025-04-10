package checkme.config

import org.http4k.cloudnative.env.Environment
import org.http4k.cloudnative.env.EnvironmentKey
import org.http4k.lens.boolean
import org.http4k.lens.int
import org.http4k.lens.string

class WebConfig (
    val port: Int,
    val domain: String,
    val hotReloadTemplates: Boolean,
) {
    companion object {
        fun fromEnvironment(environment: Environment) =
            WebConfig(
                port = portLens(environment),
                domain = domainLens(environment),
                hotReloadTemplates = hotReloadTemplatesLens(environment),
            )

        private val portLens = EnvironmentKey.int().defaulted(
            "web.port",
            8000,
            "Port the application will be listen to",
        )

        private val domainLens = EnvironmentKey.string().defaulted(
            "web.domain",
            "localhost",
            "Application public domain name",
        )

        private val hotReloadTemplatesLens = EnvironmentKey.boolean().defaulted(
            "web.hotReloadTemplates",
            false,
            "True if the application is started in development mode",
        )
    }
}
