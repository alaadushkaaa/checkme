package checkme.domain.operations.dependencies.users

import checkme.domain.accounts.Role
import checkme.domain.models.User
import checkme.web.solution.forms.UserNameSurnameForAllResults

interface UsersDatabase {

    fun selectUserById(userId: Int): User?

    fun selectUsersByRole(userRole: Role): List<User>

    fun selectUserByLogin(login: String): User?

    fun selectAllUsers(): List<User>

    fun selectUserNameSurname(userId: Int): UserNameSurnameForAllResults?

    fun insertUser(
        login: String,
        name: String,
        surname: String,
        password: String,
        role: Role,
    ): User?
}
