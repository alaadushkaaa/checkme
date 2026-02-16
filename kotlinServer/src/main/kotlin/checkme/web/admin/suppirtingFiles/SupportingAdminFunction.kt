package checkme.web.admin.suppirtingFiles

import checkme.domain.models.User
import checkme.domain.operations.users.ModifyUserError
import checkme.domain.operations.users.UserFetchingError
import checkme.domain.operations.users.UserOperationHolder
import checkme.web.auth.handlers.FetchingUserError
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success

internal fun selectUser(
    userId: Int,
    userOperations: UserOperationHolder
): Result<User, FetchingUserError> {
    return when (
        val user = userOperations.fetchUserById(userId)
    ) {
        is Success -> Success(user.value)
        is Failure -> when (user.reason) {
            UserFetchingError.NO_SUCH_USER -> Failure(FetchingUserError.NO_SUCH_USER)
            UserFetchingError.UNKNOWN_DATABASE_ERROR -> Failure(FetchingUserError.UNKNOWN_DATABASE_ERROR)
        }
    }
}

internal fun changeUserPassword(
    user: User,
    userOperations: UserOperationHolder
): Result<User, UserChangingError> {
    return when (
        val updatedUser = userOperations.modifyUserPassword(user)
    ) {
        is Success -> Success(updatedUser.value)
        is Failure -> when (updatedUser.reason) {
            ModifyUserError.UNKNOWN_DATABASE_ERROR -> Failure(UserChangingError.UNKNOWN_DATABASE_ERROR)
        }
    }

}

enum class UserChangingError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Something happened. Please try again later or ask for help"),
}