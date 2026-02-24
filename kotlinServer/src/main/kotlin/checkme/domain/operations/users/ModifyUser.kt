package checkme.domain.operations.users

import checkme.config.AppConfig
import checkme.domain.accounts.PasswordHasher
import checkme.domain.models.User
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success

class ModifyUserPassword(
    private val updateUserPassword: (
        user: User,
    ) -> User?,
    config: AppConfig,
) : (User) -> Result<User, ModifyUserError> {
    private val hasher = PasswordHasher(config.authConfig)

    override fun invoke(user: User): Result<User, ModifyUserError> =
        when (
            val editedUser = updateUserPassword(user.copy(password = hasher.hash(password = user.password)))
        ) {
            is User -> Success(editedUser)
            else -> Failure(ModifyUserError.UNKNOWN_DATABASE_ERROR)
        }
}

enum class ModifyUserError {
    UNKNOWN_DATABASE_ERROR,
}
