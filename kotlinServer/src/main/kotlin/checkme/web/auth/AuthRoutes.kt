package checkme.web.auth

import checkme.config.AppConfig
import checkme.domain.operations.OperationHolder
import checkme.domain.tools.JWTTools
import checkme.web.auth.handlers.SignUpHandler
import checkme.web.context.ContextTools
import org.http4k.core.*
import org.http4k.routing.*

fun authRouter(
    contextTools: ContextTools,
    config: AppConfig,
    operations: OperationHolder,
    jwtTools: JWTTools,
) : RoutingHttpHandler =
    routes(
        SIGN_UP bind Method.POST to SignUpHandler(operations.userOperations, jwtTools)
    )

const val AUTH_SEGMENT = "/auth"
const val SIGN_UP = "/sign-up"