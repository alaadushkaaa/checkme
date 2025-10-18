package checkme.web.solution

import checkme.config.AppConfig
import checkme.domain.operations.OperationHolder
import checkme.web.context.ContextTools
import checkme.web.solution.handlers.CheckSolutionHandler
import checkme.web.solution.handlers.ListResultsHandler
import checkme.web.solution.handlers.ListTaskResultsHandler
import checkme.web.solution.handlers.ListUserResultsHandler
import checkme.web.solution.handlers.ResultHandler
import org.http4k.core.*
import org.http4k.routing.*

fun solutionRouter(
    operations: OperationHolder,
    contextTools: ContextTools,
    config: AppConfig,
): RoutingHttpHandler =
    routes(
        "$NEW_SOLUTION/{id}" bind Method.POST to CheckSolutionHandler(
            checkOperations = operations.checkOperations,
            taskOperations = operations.taskOperations,
            userLens = contextTools.userLens,
            checkDatabaseConfig = config.checkDatabaseConfig,
            loggingConfig = config.loggingConfig
        ),
        "/me" bind Method.GET to ListResultsHandler(
            checkOperations = operations.checkOperations,
            taskOperations = operations.taskOperations,
            userOperations = operations.userOperations,
            userLens = contextTools.userLens
        ),
        "/{checkId}" bind Method.GET to ResultHandler(
            checkOperations = operations.checkOperations,
            taskOperations = operations.taskOperations,
            userLens = contextTools.userLens
        ),
        "/all/{page}" bind Method.GET to ListResultsHandler(
            checkOperations = operations.checkOperations,
            taskOperations = operations.taskOperations,
            userOperations = operations.userOperations,
            userLens = contextTools.userLens
        ),
        "/user/{id}" bind Method.GET to ListUserResultsHandler(
            checkOperations = operations.checkOperations,
            taskOperations = operations.taskOperations,
            userOperations = operations.userOperations,
            userLens = contextTools.userLens
        ),
        "/task/{id}" bind Method.GET to ListTaskResultsHandler(
            checkOperations = operations.checkOperations,
            taskOperations = operations.taskOperations,
            userOperations = operations.userOperations,
            userLens = contextTools.userLens
        ),
    )

const val SOLUTION_SEGMENT = "/solution"
const val NEW_SOLUTION = "/new"
