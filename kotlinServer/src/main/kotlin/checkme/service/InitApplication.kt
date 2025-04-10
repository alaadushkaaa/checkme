package checkme.service

import checkme.config.AppConfig
import checkme.domain.operations.OperationHolder
import org.flywaydb.core.Flyway

fun OperationHolder.initApplication(config: AppConfig) {
    applyMigrations(config)
//    operations.initGeneralUser( jooqContext, config)
}

fun applyMigrations(config: AppConfig) {
    val flyway = Flyway
        .configure()
        .dataSource(config.databaseConfig.jdbc, config.databaseConfig.user, config.databaseConfig.password)
        .locations("classpath:ru/checkme/db/migrations")
        .cleanDisabled(true)
        .validateMigrationNaming(true)
        .load()

    if (flyway.info().pending().isNotEmpty()) flyway.migrate()
}

// private fun OperationHolder.initGeneralUser(
//    jooqContext: DSLContext,
//    config: AppConfig
// ) {
//    when()
// }
