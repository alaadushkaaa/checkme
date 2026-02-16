package checkme.web.admin.handlers

import checkme.domain.models.User
import checkme.domain.operations.users.UserOperationHolder
import checkme.web.admin.suppirtingFiles.selectUser
import checkme.web.auth.supportingFiles.PasswordGenerator
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.lenses.GeneralWebLenses.idOrNull
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.lens.RequestContextLens
import java.util.UUID

class SetSystemStudentPassword(
    private val userLens: RequestContextLens<User?>,
    private val userOperations: UserOperationHolder,
    private val passwordsGenerator: PasswordGenerator
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val user = userLens(request)
        val objectMapper = jacksonObjectMapper()
        val userIdForChangePass = request.idOrNull()

        return when {
            user == null || !user.isAdmin() ->
                objectMapper.sendBadRequestError(SetSystemPassError.USER_HAS_NOT_RIGHTS.errorText)

            userIdForChangePass == null ->
                objectMapper.sendBadRequestError(SetSystemPassError.NO_USER_ID.errorText)

            else -> {
                when (
                    val userToUpdatePassword =
                        selectUser(
                        userId = userIdForChangePass,
                        userOperations = userOperations
                    )
                ) {
                    is Failure -> objectMapper.sendBadRequestError(SetSystemStudentPassword)
                    is Success ->
                }
            }
        }
        TODO("Not yet implemented")
    }

    private fun userExists(
        userId: UUID,
    ): Boolean {
        return when (userOperations.fetchUserById(userId)) {
            is Success -> true
            is Failure -> false
        }
    }
}

enum class SetSystemPassError(val errorText: String) {
    USER_HAS_NOT_RIGHTS("User has not rights to change student passwords"),
    NO_USER_ID("No user id to change user password"),
    USER_NOT_EXISTS("User for change password does not exists")
}