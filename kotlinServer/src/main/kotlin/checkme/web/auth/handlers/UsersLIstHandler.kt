package checkme.web.auth.handlers

import checkme.domain.models.User
import checkme.domain.operations.users.UserFetchingError
import checkme.domain.operations.users.UserOperationHolder
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.solution.forms.UserDataForUsersList
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import org.http4k.core.*
import org.http4k.lens.RequestContextLens

class UsersLIstHandler(
    private val usersOperations: UserOperationHolder,
    private val userLens: RequestContextLens<User?>,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)
        return when {
            user?.isAdmin() == true -> tryFetchUsers(
                usersOperations = usersOperations,
                objectMapper = objectMapper
            )

            else -> objectMapper.sendBadRequestError(ViewUsersLIstError.USER_HAS_NOT_RIGHTS.errorText)
        }
    }
}

private fun tryFetchUsers(
    usersOperations: UserOperationHolder,
    objectMapper: ObjectMapper,
): Response {
    return when (
        val users = fetchUsersForList(usersOperations)
    ) {
        is Failure -> objectMapper.sendBadRequestError(users.reason.errorText)

        is Success -> objectMapper.sendOKResponse(users.value)
    }
}

private fun fetchUsersForList(operations: UserOperationHolder): Result<List<UserDataForUsersList>, FetchingUserError> {
    return when (
        val fetchedUsers = operations.fetchUsersDataWithoutPassword()
    ) {
        is Success -> Success(fetchedUsers.value)
        is Failure -> when (fetchedUsers.reason) {
            UserFetchingError.NO_SUCH_USER -> Failure(FetchingUserError.NO_SUCH_USER)
            UserFetchingError.UNKNOWN_DATABASE_ERROR -> Failure(FetchingUserError.UNKNOWN_DATABASE_ERROR)
        }
    }
}

enum class FetchingUserError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Something happened. Please try again later or ask for help"),
    NO_SUCH_USER("User does not exist"),
}

enum class ViewUsersLIstError(val errorText: String) {
    USER_HAS_NOT_RIGHTS("Not allowed to delete task"),
}
