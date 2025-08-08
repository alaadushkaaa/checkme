package checkme.db.users

import checkme.db.generated.enums.UserRole
import checkme.db.generated.tables.references.USERS
import checkme.db.utils.safeLet
import checkme.domain.accounts.Role
import checkme.domain.models.User
import checkme.domain.operations.dependencies.users.UsersDatabase
import checkme.web.solution.forms.UserDataForUsersList
import checkme.web.solution.forms.UserNameSurnameForAllResults
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

    override fun selectUserByLogin(login: String): User? =
        selectFromUsers(jooqContext)
            .where(USERS.LOGIN.eq(login))
            .fetchOne()
            ?.toUser()

    override fun selectUsersByRole(userRole: Role): List<User> =
        selectFromUsers(jooqContext)
            .where(USERS.ROLE.eq(userRole.asDbRole()))
            .fetch()
            .mapNotNull { record: Record ->
                record.toUser()
            }

    override fun selectUserNameSurname(userId: Int): UserNameSurnameForAllResults? =
        jooqContext
            .select(
                USERS.NAME,
                USERS.SURNAME
            ).from(USERS)
            .where(USERS.ID.eq(userId))
            .fetchOne()
            ?.let { record: Record -> record.toUserDataForAllResults() }

    override fun selectAllUsersWithoutPassword(): List<UserDataForUsersList> =
        jooqContext
            .select(
                USERS.ID,
                USERS.LOGIN,
                USERS.NAME,
                USERS.SURNAME
            ).from(USERS)
            .fetch()
            .mapNotNull { record: Record -> record.toUserDataForUsersLIst() }

    override fun insertUser(
        login: String,
        name: String,
        surname: String,
        password: String,
        role: Role,
    ): User? =
        jooqContext.insertInto(USERS)
            .set(USERS.LOGIN, login)
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
                USERS.LOGIN,
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
        this[USERS.LOGIN],
        this[USERS.NAME],
        this[USERS.SURNAME],
        this[USERS.PASSWORD],
        this[USERS.ROLE],
    ) {
            id, login, name, surname, password, role ->
        User(
            id,
            login,
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

internal fun Record.toUserDataForAllResults(): UserNameSurnameForAllResults? =
    safeLet(
        this[USERS.NAME],
        this[USERS.SURNAME]
    ) {
            name,
            surname,
        ->
        UserNameSurnameForAllResults(
            name = name,
            surname = surname
        )
    }

internal fun Record.toUserDataForUsersLIst(): UserDataForUsersList? =
    safeLet(
        this[USERS.ID],
        this[USERS.LOGIN],
        this[USERS.NAME],
        this[USERS.SURNAME]
    ) {
            id,
            login,
            name,
            surname,
        ->
        UserDataForUsersList(
            id = id.toString(),
            login = login,
            name = name,
            surname = surname
        )
    }
