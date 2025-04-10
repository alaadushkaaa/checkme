package checkme.web

import org.http4k.core.*
import org.http4k.routing.*

private fun createMainRouter() =
    routes(
        "/" bind Method.GET to { _ -> ok("pong") },
    )

fun createApp(): RoutingHttpHandler {
    return createMainRouter()
}

fun ok(body: String): Response = Response(Status.OK).body(body)
