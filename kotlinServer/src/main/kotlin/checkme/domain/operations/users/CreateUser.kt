package checkme.domain.operations.users

import checkme.config.AppConfig
import checkme.domain.accounts.PasswordHasher
import checkme.domain.accounts.Role
import checkme.domain.models.User
import checkme.domain.models.ValidateUserResult
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success

class CreateUser(
    private val insertUser: (
        login: String,
        name: String,
        surname: String,
        password: String,
        role: Role,
    ) -> User?,
    private val fetchUserByLogin: (String) -> User?,
    config: AppConfig,
) : (String, String, String, String, Role) -> Result4k<User, UserCreationError> {
    private val hasher = PasswordHasher(config.authConfig)

    @Suppress("CyclomaticComplexMethod")
    override operator fun invoke(
        login: String,
        name: String,
        surname: String,
        password: String,
        role: Role,
    ): Result4k<User, UserCreationError> =
        when (User.validateUser(login, name, surname, password)) {
            ValidateUserResult.NAME_IS_TOO_LONG -> Failure(UserCreationError.NAME_IS_TOO_LONG)
            ValidateUserResult.NAME_IS_BLANK_OR_EMPTY -> Failure(UserCreationError.NAME_IS_BLANK_OR_EMPTY)
            ValidateUserResult.NAME_PATTERN_MISMATCH -> Failure(UserCreationError.NAME_PATTERN_MISMATCH)
            ValidateUserResult.SURNAME_IS_TOO_LONG -> Failure(UserCreationError.SURNAME_IS_TOO_LONG)
            ValidateUserResult.SURNAME_IS_BLANK_OR_EMPTY -> Failure(UserCreationError.SURNAME_IS_BLANK_OR_EMPTY)
            ValidateUserResult.SURNAME_PATTERN_MISMATCH -> Failure(UserCreationError.SURNAME_PATTERN_MISMATCH)
            ValidateUserResult.LOGIN_IS_TOO_LONG -> Failure(UserCreationError.LOGIN_IS_TOO_LONG)
            ValidateUserResult.LOGIN_IS_BLANK_OR_EMPTY -> Failure(UserCreationError.LOGIN_IS_BLANK_OR_EMPTY)
            ValidateUserResult.LOGIN_PATTERN_MISMATCH -> Failure(UserCreationError.LOGIN_PATTERN_MISMATCH)
            ValidateUserResult.PASSWORD_IS_BLANK_OR_EMPTY -> Failure(UserCreationError.PASSWORD_IS_BLANK_OR_EMPTY)
            ValidateUserResult.ALL_OK -> when {
                loginAlreadyExists(login) -> Failure(UserCreationError.LOGIN_ALREADY_EXISTS)
                else -> when (
                    val newUser =
                        insertUser(
                            login,
                            name,
                            surname,
                            hasher.hash(password),
                            role,
                        )
                ) {
                    is User -> Success(newUser)
                    else -> Failure(UserCreationError.UNKNOWN_DATABASE_ERROR)
                }
            }
        }

    private fun loginAlreadyExists(login: String): Boolean =
        when (fetchUserByLogin(login)) {
            is User -> true
            else -> false
        }
}

enum class UserCreationError {
    UNKNOWN_DATABASE_ERROR,
    LOGIN_ALREADY_EXISTS,
    NAME_IS_BLANK_OR_EMPTY,
    NAME_IS_TOO_LONG,
    NAME_PATTERN_MISMATCH,
    LOGIN_IS_BLANK_OR_EMPTY,
    LOGIN_IS_TOO_LONG,
    LOGIN_PATTERN_MISMATCH,
    SURNAME_IS_BLANK_OR_EMPTY,
    SURNAME_IS_TOO_LONG,
    SURNAME_PATTERN_MISMATCH,
    PASSWORD_IS_BLANK_OR_EMPTY,
}
