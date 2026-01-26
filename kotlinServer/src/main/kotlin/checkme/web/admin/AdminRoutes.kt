package checkme.web.admin

import checkme.web.admin.handlers.JournalHandler
import checkme.web.admin.handlers.LogFileHandler
import checkme.web.context.ContextTools
import org.http4k.core.Method
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes

fun adminRoutes(contextTools: ContextTools): RoutingHttpHandler =
    routes(
        "$JOURNAL/file/{fileName}" bind Method.GET to LogFileHandler(contextTools.userLens),
        "$JOURNAL/{page}" bind Method.GET to JournalHandler(contextTools.userLens)
    )

const val ADMIN_SEGMENT = "/admin"
const val JOURNAL = "/journal"
