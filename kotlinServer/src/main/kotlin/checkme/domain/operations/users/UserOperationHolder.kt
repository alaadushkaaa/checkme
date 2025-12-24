package checkme.domain.operations.users

import checkme.config.AppConfig
import checkme.domain.accounts.Role
import checkme.domain.models.User
import checkme.domain.operations.dependencies.users.UsersDatabase
import checkme.web.solution.forms.UserDataForUsersList
import checkme.web.solution.forms.UserNameSurnameForAllResults
import dev.forkhandles.result4k.Result4k
import java.util.UUID

class UserOperationHolder(
    private val usersDatabase: UsersDatabase,
    config: AppConfig,
) {
    val fetchUserById: (UUID) -> Result4k<User, UserFetchingError> =
        FetchUserById { userId: UUID -> usersDatabase.selectUserById(userId) }

    val fetchUsersByRole: (Role) -> Result4k<List<User>, UserFetchingError> =
        FetchUsersByRole { role: Role -> usersDatabase.selectUsersByRole(role) }

    val fetchUserByLogin: (String) -> Result4k<User, UserFetchingError> =
        FetchUserByLogin { login: String -> usersDatabase.selectUserByLogin(login) }

    val fetchUserNameSurname: (UUID) -> Result4k<UserNameSurnameForAllResults, UserFetchingError> =
        FetchUserNameSurname { userId: UUID -> usersDatabase.selectUserNameSurname(userId) }

    val fetchUsersDataWithoutPassword: () -> Result4k<List<UserDataForUsersList>, UserFetchingError> =
        FetchUsersDataWithoutPassword { usersDatabase.selectAllUsersWithoutPassword() }

    val createUser: (String, String, String, String, Role) -> Result4k<User, UserCreationError> =
        CreateUser(
            insertUser = { login: String, name: String, surname: String, password: String, role: Role ->
                usersDatabase
                    .insertUser(login = login, name = name, surname = surname, password = password, role = role)
            },
            fetchUserByLogin = usersDatabase::selectUserByLogin,
            config = config
        )
}
