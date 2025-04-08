package checkme

import checkme.config.AppConfig
import checkme.db.utils.createJooqContext
import checkme.domain.operations.OperationHolder
import checkme.service.initApplication
import checkme.web.createApp
import org.http4k.server.Netty
import org.http4k.server.asServer

fun main() {
    val config = AppConfig.fromEnvironment()
    val jooqContext = createJooqContext(config.databaseConfig)
    val operations = OperationHolder(config)

    operations.initApplication(operations, jooqContext, config)

    val app = createApp(operations, config)
    val server = app.asServer(Netty(config.webConfig.port)).start()
    println("Running on port http://localhost:${server.port()}/")
}
