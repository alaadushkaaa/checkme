package checkme.domain.operations.users

import checkme.domain.models.User
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success

class ModifyUserPassword(
    private val updateUserPassword: (
        user: User,
    ) -> User?,
) : (User) -> Result<User, ModifyUserError> {
    override fun invoke(user: User): Result<User, ModifyUserError> =
        when (
            val editedUser = updateUserPassword(user)
        ) {
            is User -> Success(editedUser)
            else -> Failure(ModifyUserError.UNKNOWN_DATABASE_ERROR)
        }
}

enum class ModifyUserError {
    UNKNOWN_DATABASE_ERROR,
}