package checkme.domain.operations

import checkme.config.AppConfig
import checkme.domain.operations.checks.CheckOperationHolder
import checkme.domain.operations.dependencies.DatabaseOperations
import checkme.domain.operations.users.UserOperationHolder

class OperationHolder (
    database: DatabaseOperations,
    config: AppConfig,
) {
    val userOperations: UserOperationHolder = UserOperationHolder(
        database.userOperations,
        config,
    )

    val checkOperations: CheckOperationHolder = CheckOperationHolder(database.checkOperations, config)
}
