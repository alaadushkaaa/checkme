package checkme.web.filters

import checkme.web.internalServerError
import org.http4k.core.*
import org.http4k.filter.ServerFilters
import org.slf4j.LoggerFactory
import checkme.MAIN_CLASS

private val logger = LoggerFactory.getLogger(MAIN_CLASS)

fun catchAndLogExceptionsFilter(): Filter =
    ServerFilters.CatchAll { throwable ->
        logger
            .atError()
            .setMessage("\n${throwable.stackTraceToString()}")
            .log()
        internalServerError
    }
