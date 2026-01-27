package checkme.domain.operations.dependencies.users

import checkme.domain.accounts.Role
import checkme.domain.models.User
import checkme.web.solution.forms.UserDataForUsersList
import checkme.web.solution.forms.UserNameSurnameForAllResults
import java.util.UUID

interface UsersDatabase {

    fun selectUserById(userId: UUID): User?

    fun selectUsersByRole(userRole: Role): List<User>

    fun selectUserByLogin(login: String): User?

    fun selectAllUsers(): List<User>

    fun selectUserNameSurname(userId: UUID): UserNameSurnameForAllResults?

    fun selectAllUsersWithoutPassword(): List<UserDataForUsersList>

    fun insertUser(
        login: String,
        name: String,
        surname: String,
        password: String,
        role: Role,
    ): User?
}
