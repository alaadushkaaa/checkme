package checkme.web.auth.handlers

import checkme.domain.accounts.Role
import checkme.domain.models.User
import checkme.domain.operations.users.UserCreationError
import checkme.domain.operations.users.UserOperationHolder
import checkme.domain.tools.JWTTools
import checkme.web.auth.forms.SignUpRequest
import checkme.web.auth.forms.UserAuthResponse
import checkme.web.commonExtensions.sendBadRequestError
import checkme.web.commonExtensions.sendStatusCreated
import checkme.web.commonExtensions.sendStatusUnauthorized
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.failureOrNull
import org.http4k.core.*

class SignUpHandler(
    private val userOperations: UserOperationHolder,
    private val jwtTools: JWTTools,
) : HttpHandler {
    @Suppress("LongMethod")
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val signUpRequest = objectMapper.readValue<SignUpRequest>(request.bodyString())
        return when (val userInsertResult = tryInsert(form = signUpRequest, userOperations = userOperations)) {
            is Failure -> objectMapper.sendStatusUnauthorized(userInsertResult.reason.errorText)

            is Success -> {
                when (val tokenResult = jwtTools.createUserJwt(userInsertResult.value.id)) {
                    is Failure -> objectMapper.sendBadRequestError(SignUpError.TOKEN_CREATION_ERROR.errorText)

                    is Success -> {
                        val signUpUserResponse = UserAuthResponse(
                            signUpRequest.username,
                            signUpRequest.name,
                            signUpRequest.surname,
                            userInsertResult.value.role.name,
                            tokenResult.value
                        )
                        objectMapper.sendStatusCreated(signUpUserResponse)
                    }
                }
            }
        }
    }

    @Suppress("CyclomaticComplexMethod")
    private fun tryInsert(
        form: SignUpRequest,
        userOperations: UserOperationHolder,
    ): Result<User, SignUpError> {
        val login = form.username.trim()
        val name = form.name
        val surname = form.surname
        val password = form.password.trim()
        return when (
            val result = userOperations
                .createUser(login, name, surname, password, Role.STUDENT)
        ) {
            is Success -> Success(result.value)
            is Failure -> Failure(
                when (result.failureOrNull()) {
                    UserCreationError.LOGIN_ALREADY_EXISTS -> SignUpError.LOGIN_ALREADY_EXISTS
                    UserCreationError.LOGIN_IS_TOO_LONG -> SignUpError.LOGIN_IS_TOO_LONG
                    UserCreationError.LOGIN_IS_BLANK_OR_EMPTY -> SignUpError.LOGIN_IS_BLANK_OR_EMPTY
                    UserCreationError.LOGIN_PATTERN_MISMATCH -> SignUpError.LOGIN_PATTERN_MISMATCH
                    UserCreationError.NAME_IS_TOO_LONG -> SignUpError.NAME_IS_TOO_LONG
                    UserCreationError.NAME_IS_BLANK_OR_EMPTY -> SignUpError.NAME_IS_BLANK_OR_EMPTY
                    UserCreationError.NAME_PATTERN_MISMATCH -> SignUpError.NAME_PATTERN_MISMATCH
                    UserCreationError.SURNAME_PATTERN_MISMATCH -> SignUpError.SURNAME_PATTERN_MISMATCH
                    UserCreationError.SURNAME_IS_TOO_LONG -> SignUpError.SURNAME_IS_TOO_LONG
                    UserCreationError.SURNAME_IS_BLANK_OR_EMPTY -> SignUpError.SURNAME_IS_BLANK_OR_EMPTY
                    UserCreationError.PASSWORD_IS_BLANK_OR_EMPTY -> SignUpError.PASSWORD_IS_BLANK_OR_EMPTY

                    else -> SignUpError.UNKNOWN_DATABASE_ERROR
                }
            )
        }
    }
}

enum class SignUpError(val errorText: String) {
    LOGIN_ALREADY_EXISTS("Login already exists"),
    UNKNOWN_DATABASE_ERROR("Something happened. Please try again later or ask for help"),
    TOKEN_CREATION_ERROR("Something happened with token. Please try again later or ask for help"),
    NAME_IS_BLANK_OR_EMPTY("The name can not be empty"),
    NAME_IS_TOO_LONG("Maximum name length - ${User.MAX_LENGTH}"),
    NAME_PATTERN_MISMATCH("The name must contain only cyrillic characters, spaces and hyphens"),
    LOGIN_IS_BLANK_OR_EMPTY("Login can not be empty"),
    LOGIN_IS_TOO_LONG("Maximum login length - ${User.MAX_LENGTH}"),
    LOGIN_PATTERN_MISMATCH("Login must contain only cyrillic characters, numbers and signs"),
    SURNAME_IS_BLANK_OR_EMPTY("The last name can not be empty"),
    SURNAME_IS_TOO_LONG("Maximum last name length - ${User.MAX_LENGTH}"),
    SURNAME_PATTERN_MISMATCH("The name must contain only cyrillic characters, spaces and hyphens"),
    PASSWORD_IS_BLANK_OR_EMPTY("Password can not be empty"),
}
