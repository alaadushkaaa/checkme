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
    val database = DatabaseOperationsHolder(jooqContext)
    val operations = OperationHolder(database, config)

    operations.initApplication(config)

    val app = createApp()
    val server = app.asServer(Netty(config.webConfig.port)).start()
    println("Running on port http://localhost:${server.port()}/")
}
