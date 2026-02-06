package checkme.web.auth

import checkme.config.AppConfig
import checkme.domain.operations.OperationHolder
import checkme.domain.tools.JWTTools
import checkme.web.auth.handlers.LoadStudentsHandler
import checkme.web.auth.handlers.SignInHandler
import checkme.web.auth.handlers.SignUpHandler
import checkme.web.auth.handlers.UsersLIstHandler
import checkme.web.auth.supportingFiles.PasswordGenerator
import checkme.web.context.ContextTools
import org.http4k.core.*
import org.http4k.routing.*

fun authRouter(
    config: AppConfig,
    operations: OperationHolder,
    jwtTools: JWTTools,
    contextTools: ContextTools,
): RoutingHttpHandler =
    routes(
        SIGN_UP bind Method.POST to SignUpHandler(operations.userOperations, jwtTools),
        SIGN_IN bind Method.POST to SignInHandler(operations.userOperations, config.authConfig, jwtTools),
        "/all" bind Method.GET to UsersLIstHandler(operations.userOperations, contextTools.userLens),
        AUTOMATIC_REGISTRATION bind Method.POST to LoadStudentsHandler(
            userLens = contextTools.userLens,
            userOperations = operations.userOperations,
            passwordGenerator = PasswordGenerator(config.authConfig.seed)
        )
    )

const val AUTH_SEGMENT = "/user"
const val AUTOMATIC_REGISTRATION = "/automatic-registration"
const val SIGN_UP = "/sign_up"
const val SIGN_IN = "/sign_in"
