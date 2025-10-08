package checkme.web

import checkme.config.AppConfig
import checkme.domain.operations.OperationHolder
import checkme.domain.tools.JWTTools
import checkme.web.auth.AUTH_SEGMENT
import checkme.web.auth.authRouter
import checkme.web.context.ContextTools
import checkme.web.filters.FiltersHolder
import checkme.web.solution.SOLUTION_SEGMENT
import checkme.web.solution.solutionRouter
import checkme.web.tasks.TASK_SEGMENT
import checkme.web.tasks.taskRouter
import org.http4k.core.*
import org.http4k.routing.*

private fun createMainRouter(
    operations: OperationHolder,
    config: AppConfig,
    jwtTools: JWTTools,
    contextTools: ContextTools,
) = routes(
    "/" bind Method.GET to { _ -> ok("pong") },
    AUTH_SEGMENT bind authRouter(
        config = config,
        operations = operations,
        jwtTools = jwtTools,
        contextTools = contextTools
    ),
    SOLUTION_SEGMENT bind solutionRouter(
        operations = operations,
        contextTools = contextTools,
        config = config
    ),
    TASK_SEGMENT bind taskRouter(
        operations = operations,
        contextTools = contextTools,
        config = config
    ),
)

fun createApp(
    operations: OperationHolder,
    config: AppConfig,
    contextTools: ContextTools,
): RoutingHttpHandler {
    val jwtTools = JWTTools(config.authConfig.secret, "checkme")
    val filters = FiltersHolder(
        contextTools = contextTools,
        operations = operations,
        jwtTools = jwtTools,
        config = config
    )
    val app = filters.all
        .then(
            createMainRouter(
                operations = operations,
                config = config,
                jwtTools = jwtTools,
                contextTools = contextTools,
            )
        )
    return app
}

val internalServerError: Response = Response(Status.INTERNAL_SERVER_ERROR)

fun ok(body: String): Response = Response(Status.OK).body(body)
