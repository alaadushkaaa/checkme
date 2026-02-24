package checkme.web.admin.handlers

import checkme.domain.accounts.PasswordHasher
import checkme.domain.accounts.Role
import checkme.domain.models.User
import checkme.domain.operations.users.UserFetchingError
import checkme.domain.operations.users.UserOperationHolder
import checkme.web.admin.forms.UserInfo
import checkme.web.auth.supportingFiles.PasswordGenerator
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.lens.RequestContextLens
import kotlin.collections.map

class GetUsersInfoHandler(
    private val userOperations: UserOperationHolder,
    private val userLens: RequestContextLens<User?>,
    private val passwordGenerator: PasswordGenerator,
    private val hasher: PasswordHasher,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val user = userLens(request)
        return when {
            user?.isAdmin() == true -> tryFetchUsersData(
                usersOperations = userOperations,
                objectMapper = objectMapper
            )

            else -> objectMapper.sendBadRequestError(ViewUsersDataError.USER_HAS_NOT_RIGHTS.errorText)
        }
    }

    private fun tryFetchUsersData(
        usersOperations: UserOperationHolder,
        objectMapper: ObjectMapper,
    ): Response {
        return when (
            val users = fetchUsersData(usersOperations)
        ) {
            is Failure -> objectMapper.sendBadRequestError(users.reason.errorText)

            is Success -> {
                val usersInfo = users.value.map { user ->
                    var isSystemPass = true
                    if (user.password != hasher.hash(passwordGenerator.generateStudentPass(user.login))) {
                        isSystemPass = false
                    }
                    UserInfo(
                        id = user.id,
                        name = user.name,
                        surname = user.surname,
                        login = user.login,
                        isSystemPass = isSystemPass
                    )
                }
                objectMapper.sendOKResponse(usersInfo)
            }
        }
    }

    private fun fetchUsersData(operations: UserOperationHolder): Result<List<User>, FetchingUserDataError> {
        return when (
            val fetchedUsers = operations.fetchUsersByRole(Role.STUDENT)
        ) {
            is Success -> Success(fetchedUsers.value)
            is Failure -> when (fetchedUsers.reason) {
                UserFetchingError.NO_SUCH_USER -> Failure(FetchingUserDataError.NO_SUCH_USER)
                UserFetchingError.UNKNOWN_DATABASE_ERROR -> Failure(FetchingUserDataError.UNKNOWN_DATABASE_ERROR)
            }
        }
    }
}

enum class FetchingUserDataError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Something happened. Please try again later or ask for help"),
    NO_SUCH_USER("User does not exist"),
}

enum class ViewUsersDataError(val errorText: String) {
    USER_HAS_NOT_RIGHTS("Not allowed to view users data"),
}
