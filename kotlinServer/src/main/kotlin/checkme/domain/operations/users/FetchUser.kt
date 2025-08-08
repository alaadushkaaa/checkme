package checkme.domain.operations.users

import checkme.domain.accounts.Role
import checkme.domain.models.User
import checkme.web.solution.forms.UserDataForUsersList
import checkme.web.solution.forms.UserNameSurnameForAllResults
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import org.jooq.exception.DataAccessException

class FetchUserById(
    private val fetchUserByID: (Int) -> User?,
) : (Int) -> Result4k<User, UserFetchingError> {

    override operator fun invoke(userId: Int): Result4k<User, UserFetchingError> =
        try {
            when (val user = fetchUserByID(userId)) {
                is User -> Success(user)
                else -> Failure(UserFetchingError.NO_SUCH_USER)
            }
        } catch (_: DataAccessException) {
            Failure(UserFetchingError.UNKNOWN_DATABASE_ERROR)
        }
}

class FetchUsersByRole(
    private val fetchUsersByRole: (Role) -> List<User>?,
) : (Role) -> Result4k<List<User>, UserFetchingError> {

    override operator fun invoke(role: Role): Result4k<List<User>, UserFetchingError> =
        try {
            when (val users = fetchUsersByRole(role)) {
                is List<User> -> Success(users)
                else -> Failure(UserFetchingError.NO_SUCH_USER)
            }
        } catch (_: DataAccessException) {
            Failure(UserFetchingError.UNKNOWN_DATABASE_ERROR)
        }
}

class FetchUserByLogin(
    private val fetchUsersByLogin: (String) -> User?,
) : (String) -> Result4k<User, UserFetchingError> {

    override operator fun invoke(login: String): Result4k<User, UserFetchingError> =
        try {
            when (val user = fetchUsersByLogin(login)) {
                is User -> Success(user)
                else -> Failure(UserFetchingError.NO_SUCH_USER)
            }
        } catch (_: DataAccessException) {
            Failure(UserFetchingError.UNKNOWN_DATABASE_ERROR)
        }
}

class FetchUserNameSurname(
    private val fetchUserNameSurname: (Int) -> UserNameSurnameForAllResults?,
) : (Int) -> Result4k<UserNameSurnameForAllResults, UserFetchingError> {
    override operator fun invoke(userId: Int): Result4k<UserNameSurnameForAllResults, UserFetchingError> =
        try {
            when (val userData = fetchUserNameSurname(userId)) {
                is UserNameSurnameForAllResults -> Success(userData)
                else -> Failure(UserFetchingError.NO_SUCH_USER)
            }
        } catch (_: DataAccessException) {
            Failure(UserFetchingError.UNKNOWN_DATABASE_ERROR)
        }
}

class FetchUsersDataWithoutPassword(
    private val fetchUserDataWithoutPassword: () -> List<UserDataForUsersList>?,
) : () -> Result4k<List<UserDataForUsersList>, UserFetchingError> {
    override operator fun invoke(): Result4k<List<UserDataForUsersList>, UserFetchingError> =
        try {
            when (val userData = fetchUserDataWithoutPassword()) {
                is List<UserDataForUsersList> -> Success(userData)
                else -> Failure(UserFetchingError.NO_SUCH_USER)
            }
        } catch (_: DataAccessException) {
            Failure(UserFetchingError.UNKNOWN_DATABASE_ERROR)
        }
}

class FetchAllUsers(
    private val fetchAllUsers: () -> List<User>?,
) : () -> Result4k<List<User>, UserFetchingError> {

    override operator fun invoke(): Result4k<List<User>, UserFetchingError> =
        try {
            when (val users = fetchAllUsers()) {
                is List<User> -> Success(users)
                else -> Failure(UserFetchingError.NO_SUCH_USER)
            }
        } catch (_: DataAccessException) {
            Failure(UserFetchingError.UNKNOWN_DATABASE_ERROR)
        }
}

enum class UserFetchingError {
    UNKNOWN_DATABASE_ERROR,
    NO_SUCH_USER,
}
