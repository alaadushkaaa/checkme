package checkme.db.users

import checkme.db.generated.enums.UserRole
import checkme.db.generated.tables.references.USERS
import checkme.db.utils.safeLet
import checkme.domain.accounts.Role
import checkme.domain.models.User
import checkme.domain.operations.dependencies.UsersDatabase
import org.jooq.DSLContext
import org.jooq.Record

class UserOperations (
    private val jooqContext: DSLContext,
) : UsersDatabase {
    override fun selectAllUsers(): List<User> =
        selectFromUsers(jooqContext)
            .fetch()
            .mapNotNull { record: Record ->
                record.toUser()
            }

    override fun selectUserById(userId: Int): User? =
        selectFromUsers(jooqContext)
            .where(USERS.ID.eq(userId))
            .fetchOne()
            ?.toUser()

    override fun selectUsersByRole(userRole: Role): List<User> =
        selectFromUsers(jooqContext)
            .where(USERS.ROLE.eq(userRole.asDbRole()))
            .fetch()
            .mapNotNull { record: Record ->
                record.toUser()
            }

    override fun insertUser(
        name: String,
        surname: String,
        password: String,
        role: Role,
    ): User? =
        jooqContext.insertInto(USERS)
            .set(USERS.NAME, name)
            .set(USERS.SURNAME, surname)
            .set(USERS.PASSWORD, password)
            .set(USERS.ROLE, UserRole.valueOf(role.toString()))
            .returningResult()
            .fetchOne()
            ?.toUser()

    private fun selectFromUsers(jooqContext: DSLContext) =
        jooqContext
            .select(
                USERS.ID,
                USERS.NAME,
                USERS.SURNAME,
                USERS.PASSWORD,
                USERS.ROLE,
            )
            .from(USERS)
}

internal fun Record.toUser(): User? =
    safeLet(
        this[USERS.ID],
        this[USERS.NAME],
        this[USERS.SURNAME],
        this[USERS.PASSWORD],
        this[USERS.ROLE],
    ) {
            id, name, surname, password, role ->
        User(
            id,
            name,
            surname,
            password,
            Role.valueOf(role.toString())
        )
    }

internal fun Role.asDbRole(): UserRole? =
    when (this) {
        Role.STUDENT -> UserRole.STUDENT
        Role.ADMIN -> UserRole.ADMIN
        else -> null
    }
