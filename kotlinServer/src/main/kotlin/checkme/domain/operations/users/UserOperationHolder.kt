package checkme.domain.operations.users

import checkme.config.AppConfig
import checkme.domain.accounts.Role
import checkme.domain.models.User
import checkme.domain.operations.dependencies.UsersDatabase
import dev.forkhandles.result4k.Result4k

class UserOperationHolder(
    private val usersDatabase: UsersDatabase,
    config: AppConfig,
) {
    val fetchUserById: (Int) -> Result4k<User, UserFetchingError> =
        FetchUserById { userId: Int -> usersDatabase.selectUserById(userId) }

    val fetchUsersByRole: (Role) -> Result4k<List<User>, UserFetchingError> =
        FetchUsersByRole { role: Role -> usersDatabase.selectUsersByRole(role) }

    val createUser: (String, String, String, Role) -> Result4k<User, UserCreationError> =
        CreateUser(
            insertUser = { name: String, surname: String, password: String, role: Role ->
                usersDatabase.insertUser(name = name, surname = surname, password = password, role = role)
            },
            config
        )
}
