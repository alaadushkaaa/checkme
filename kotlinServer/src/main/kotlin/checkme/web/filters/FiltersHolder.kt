package checkme.web.filters

import checkme.config.AppConfig
import checkme.domain.operations.OperationHolder
import checkme.domain.tools.JWTTools
import checkme.web.context.ContextTools
import org.http4k.core.*
import org.http4k.filter.ServerFilters

class FiltersHolder(
    contextTools: ContextTools,
    operations: OperationHolder,
    jwtTools: JWTTools,
    config: AppConfig,
) {
    val initContext = ServerFilters.InitialiseRequestContext(contextTools.appContexts)
    val setAuthUsersToContext = AddUserToContextFilter(
        userLens = contextTools.userLens,
        userOperations = operations.userOperations,
        jwtTools = jwtTools,
    )
    val catchAndLogExceptionsFilter = catchAndLogExceptionsFilter()
    val corsFilter = corsFilter(config)

    val all = catchAndLogExceptionsFilter
        .then(corsFilter)
        .then(initContext)
        .then(setAuthUsersToContext)
}
