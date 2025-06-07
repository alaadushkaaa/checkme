package checkme.web

import checkme.config.AppConfig
import checkme.domain.operations.OperationHolder
import checkme.domain.tools.JWTTools
import checkme.web.auth.AUTH_SEGMENT
import checkme.web.auth.authRouter
import checkme.web.filters.catchAndLogExceptionsFilter
import checkme.web.filters.corsFilter
import checkme.web.solution.SOLUTION_SEGMENT
import checkme.web.solution.handlers.task
import checkme.web.solution.solutionRouter
import org.http4k.core.*
import org.http4k.routing.*

private fun createMainRouter(
    operations: OperationHolder,
    config: AppConfig,
    jwtTools: JWTTools,
) = routes(
    "/" bind Method.GET to { _ -> ok("pong") },
    AUTH_SEGMENT bind authRouter(config = config, operations = operations, jwtTools = jwtTools),
    SOLUTION_SEGMENT bind solutionRouter(operations = operations),
    // todo необходимо создать базу данных заданий, реализовать страницы
    // todo для каждого задания создается папка - внутри нее файлы-проверки, относящиеся к заданию
    "/task/68133a0cd4c0f72629c8893f" bind Method.GET to { _ ->
        ok(
            """
            {
                "name": "${task.name}",
                "answerFormat": "${task.answerFormat}",
                "description": "${task.description}"
            }
            """.trimIndent()
        )
    }
)

fun createApp(
    operations: OperationHolder,
    config: AppConfig,
): RoutingHttpHandler {
    val jwtTools = JWTTools(config.authConfig.secret, "checkme")
    val app = createFilters(
        config = config
    ).then(
        createMainRouter(operations, config, jwtTools)
    )
    return app
}

private fun createFilters(config: AppConfig): Filter {
    val catchAndLogExceptionsFilter = catchAndLogExceptionsFilter()
    val corsFilter = corsFilter(config)
    return catchAndLogExceptionsFilter.then(corsFilter)
}

val internalServerError: Response = Response(Status.INTERNAL_SERVER_ERROR)

fun ok(body: String): Response = Response(Status.OK).body(body)
