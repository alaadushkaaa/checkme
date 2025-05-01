package checkme.db

import checkme.db.checks.CheckOperations
import checkme.db.users.UserOperations
import checkme.domain.operations.dependencies.ChecksDatabase
import checkme.domain.operations.dependencies.DatabaseOperations
import checkme.domain.operations.dependencies.UsersDatabase
import org.jooq.DSLContext

@Suppress("detekt:UnusedPrivateProperty")
class DatabaseOperationsHolder(
    jooqContext: DSLContext,
) : DatabaseOperations {
    private val userOperationsInternal = UserOperations(jooqContext)
    private val checkOperationInternal = CheckOperations(jooqContext)

    override val userOperations: UsersDatabase get() = userOperationsInternal

    override val checkOperations: ChecksDatabase get() = checkOperationInternal
}
