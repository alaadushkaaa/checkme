package checkme.service

import checkme.config.AppConfig
import checkme.config.CheckDatabaseConfig
import checkme.domain.operations.OperationHolder
import org.flywaydb.core.Flyway
import java.sql.DriverManager

fun OperationHolder.initApplication(config: AppConfig) {
    getConnectionToChecksDatabase(config.checkDatabaseConfig)
    applyMigrations(config)
    this.initGeneralUser(config)
}

fun applyMigrations(config: AppConfig) {
    val flyway = Flyway
        .configure()
        .dataSource(config.databaseConfig.jdbc, config.databaseConfig.user, config.databaseConfig.password)
        .locations("classpath:checkme/db/migrations")
        .cleanDisabled(true)
        .validateMigrationNaming(true)
        .load()

    if (flyway.info().pending().isNotEmpty()) flyway.migrate()
}

@Suppress("TooGenericExceptionCaught")
private fun getConnectionToChecksDatabase(config: CheckDatabaseConfig) {
    try {
        DriverManager.getConnection(config.jdbc, config.user, config.password).use {
        }
    } catch (e: Exception) {
        error("Cannot connect to checks database: $e")
    }
}
