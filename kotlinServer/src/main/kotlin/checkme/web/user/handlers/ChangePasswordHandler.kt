package checkme.web.user.handlers

import checkme.domain.accounts.PasswordHasher
import checkme.domain.models.User
import checkme.domain.operations.users.UserOperationHolder
import checkme.logging.LoggerType
import checkme.logging.ServerLogger
import checkme.web.admin.suppirtingFiles.changeUserPassword
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendOKResponse
import checkme.web.lenses.ChangePasswordLenses
import checkme.web.user.forms.ChangePasswordData
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.lens.MultipartForm
import org.http4k.lens.RequestContextLens

class ChangePasswordHandler(
    private val userLens: RequestContextLens<User?>,
    private val userOperations: UserOperationHolder,
    private val passwordHasher: PasswordHasher,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val user = userLens(request)
        val objectMapper = jacksonObjectMapper()
        val form: MultipartForm = ChangePasswordLenses.multiPartFormFieldsAll(request)
        return when {
            user == null || user.isAdmin() ->
                objectMapper.sendBadRequestError(ChangeOwnPasswordError.USER_HAS_NOT_RIGHTS.errorText)

            else -> {
                val changePasswordData = ChangePasswordData(
                    oldPassword = ChangePasswordLenses.oldPasswordField(form).value,
                    newPassword = ChangePasswordLenses.newPasswordField(form).value
                )
                if (!checkOldPassword(changePasswordData.oldPassword, user)) {
                    objectMapper.sendBadRequestError(ChangeOwnPasswordError.INCORRECT_OLD_PASSWORD.errorText)
                } else {
                    tryUpdateUserPassword(
                        user = user,
                        newPassword = changePasswordData.newPassword,
                        userOperations = userOperations,
                        objectMapper = objectMapper
                    )
                }
            }
        }
    }

    private fun checkOldPassword(
        oldPassword: String,
        user: User,
    ): Boolean = passwordHasher.hash(oldPassword) == user.password

    private fun tryUpdateUserPassword(
        user: User,
        newPassword: String,
        userOperations: UserOperationHolder,
        objectMapper: ObjectMapper,
    ): Response {
        return when (
            changeUserPassword(
                user = user.copy(password = newPassword),
                userOperations = userOperations
            )
        ) {
            is Failure -> {
                ServerLogger.log(
                    user = user,
                    action = "Change user password error",
                    message = "Error while try to change user password for user ${user.id}" +
                        "${user.login}. " +
                        "Error: ${ChangeOwnPasswordError.UPDATE_PASSWORD_ERROR.errorText}",
                    type = LoggerType.INFO
                )
                objectMapper.sendBadRequestError(ChangeOwnPasswordError.UPDATE_PASSWORD_ERROR.errorText)
            }

            is Success -> objectMapper.sendOKResponse(user.id)
        }
    }
}

enum class ChangeOwnPasswordError(val errorText: String) {
    USER_HAS_NOT_RIGHTS("User has not rights to change student password"),
    INCORRECT_OLD_PASSWORD("Old user password is incorrect"),
    UPDATE_PASSWORD_ERROR("Failed to change student password"),
}
