package checkme.db

import checkme.db.users.UserOperations
import checkme.domain.operations.dependencies.DatabaseOperations
import checkme.domain.operations.dependencies.UsersDatabase
import org.jooq.DSLContext

@Suppress("detekt:UnusedPrivateProperty")
class DatabaseOperationsHolder(
    jooqContext: DSLContext,
) : DatabaseOperations {
    private val userOperationsInternal = UserOperations(jooqContext)

    override val userOperations: UsersDatabase get() = userOperationsInternal
}
