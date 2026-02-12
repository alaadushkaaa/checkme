package checkme.web.admin

import checkme.config.AppConfig
import checkme.domain.operations.OperationHolder
import checkme.web.admin.handlers.JournalHandler
import checkme.web.admin.handlers.LoadSystemPasswordsHandler
import checkme.web.admin.handlers.LogFileHandler
import checkme.web.auth.supportingFiles.PasswordGenerator
import checkme.web.context.ContextTools
import org.http4k.core.Method
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes

fun adminRoutes(
    config: AppConfig,
    contextTools: ContextTools,
    operations: OperationHolder,
): RoutingHttpHandler =
    routes(
        "$JOURNAL/file/{fileName}" bind Method.GET to LogFileHandler(contextTools.userLens),
        "$JOURNAL/{page}" bind Method.GET to JournalHandler(contextTools.userLens),
        GET_PASSWORDS bind Method.POST to LoadSystemPasswordsHandler(
            userLens = contextTools.userLens,
            userOperations = operations.userOperations,
            passwordGenerator = PasswordGenerator(config.authConfig.seed)
        )
    )

const val ADMIN_SEGMENT = "/admin"
const val JOURNAL = "/journal"
const val GET_PASSWORDS = "get-system-passwords"
