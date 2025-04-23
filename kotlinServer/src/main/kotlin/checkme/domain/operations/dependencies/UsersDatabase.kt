package checkme.domain.operations.dependencies

import checkme.domain.accounts.Role
import checkme.domain.models.User

interface UsersDatabase {

    fun selectUserById(userId: Int): User?

    fun selectUsersByRole(userRole: Role): List<User>

    fun selectUserByLogin(login: String): User?

    fun selectAllUsers(): List<User>

    fun insertUser(
        login: String,
        name: String,
        surname: String,
        password: String,
        role: Role,
    ): User?
}
