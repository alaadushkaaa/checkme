package checkme.web.admin.handlers

import checkme.domain.models.User
import checkme.domain.operations.users.UserOperationHolder
import checkme.logging.LoggerType
import checkme.logging.ServerLogger
import checkme.web.admin.suppirtingFiles.changeUserPassword
import checkme.web.admin.suppirtingFiles.selectUser
import checkme.web.auth.supportingFiles.PasswordGenerator
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.lenses.GeneralWebLenses.idOrNull
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.lens.RequestContextLens

class SetSystemStudentPasswordHandler(
    private val userLens: RequestContextLens<User?>,
    private val userOperations: UserOperationHolder,
    private val passwordsGenerator: PasswordGenerator,
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
                    is Failure -> objectMapper.sendBadRequestError(SetSystemPassError.USER_NOT_EXISTS.errorText)
                    is Success -> tryUpdateUserPassword(
                        user = user,
                        userOperations = userOperations,
                        userToUpdatePassword = userToUpdatePassword.value,
                        objectMapper = objectMapper
                    )
                }
            }
        }
    }

    private fun tryUpdateUserPassword(
        user: User,
        userOperations: UserOperationHolder,
        userToUpdatePassword: User,
        objectMapper: ObjectMapper,
    ): Response {
        val systemPassword = passwordsGenerator.generateStudentPass(userToUpdatePassword.login)
        return when (
            changeUserPassword(
                user = userToUpdatePassword.copy(password = systemPassword),
                userOperations = userOperations
            )
        ) {
            is Failure -> {
                ServerLogger.log(
                    user = user,
                    action = "Change user password error",
                    message = "Error while try to change user password for user ${userToUpdatePassword.id}" +
                        "${userToUpdatePassword.login}. " +
                        "Error: ${SetSystemPassError.UPDATE_PASSWORD_ERROR.errorText}",
                    type = LoggerType.INFO
                )
                objectMapper.sendBadRequestError(SetSystemPassError.UPDATE_PASSWORD_ERROR.errorText)
            }

            is Success -> objectMapper.sendOKResponse(userToUpdatePassword.id)
        }
    }
}

enum class SetSystemPassError(val errorText: String) {
    USER_HAS_NOT_RIGHTS("User has not rights to change student passwords"),
    NO_USER_ID("No user id to change user password"),
    UPDATE_PASSWORD_ERROR("Failed to change student password"),
    USER_NOT_EXISTS("User for change password does not exists"),
}
