package checkme.web.user

import checkme.config.AppConfig
import checkme.domain.accounts.PasswordHasher
import checkme.domain.operations.OperationHolder
import checkme.web.context.ContextTools
import checkme.web.user.handlers.ChangePasswordHandler
import org.http4k.core.Method
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes

fun userRouter(
    config: AppConfig,
    operations: OperationHolder,
    contextTools: ContextTools,
): RoutingHttpHandler =
    routes(
        CHANGE_PASS bind Method.POST to ChangePasswordHandler(
            userLens = contextTools.userLens,
            userOperations = operations.userOperations,
            passwordHasher = PasswordHasher(config.authConfig)
        )
    )

const val CHANGE_PASS = "/change-password"
const val USER_SEGMENT = "/user"
