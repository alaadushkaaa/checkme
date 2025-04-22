package checkme.domain.operations.users

import checkme.config.AppConfig
import checkme.domain.accounts.PasswordHasher
import checkme.domain.accounts.Role
import checkme.domain.models.User
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success

class CreateUser(
    private val insertUser: (
        name: String,
        surname: String,
        password: String,
        role: Role,
    ) -> User?,
    config: AppConfig,
) : (String, String, String, Role) -> Result4k<User, UserCreationError> {
    private val hasher = PasswordHasher(config.authConfig)

    override operator fun invoke(
        name: String,
        surname: String,
        password: String,
        role: Role,
    ): Result4k<User, UserCreationError> =
        when (
            val newUser =
                insertUser(
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

enum class UserCreationError {
    UNKNOWN_DATABASE_ERROR,
    LOGIN_ALREADY_EXISTS,
    INVALID_USER_DATA,
}
