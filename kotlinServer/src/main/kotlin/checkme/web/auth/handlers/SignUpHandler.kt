package checkme.web.auth.handlers

import checkme.domain.accounts.Role
import checkme.domain.models.User
import checkme.domain.operations.users.UserCreationError
import checkme.domain.operations.users.UserOperationHolder
import checkme.domain.tools.JWTTools
import checkme.web.auth.forms.SignUpRequest
import checkme.web.auth.forms.UserAuthResponse
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
            is Failure -> Response(Status.UNAUTHORIZED)
                .body(objectMapper.writeValueAsString(mapOf("error" to userInsertResult.reason.errorText)))
            is Success -> {
                when (val tokenResult = jwtTools.createUserJwt(userInsertResult.value.id)) {
                    is Failure -> Response(
                        Status.INTERNAL_SERVER_ERROR
                    ).body(objectMapper.writeValueAsString(SignUpError.TOKEN_CREATION_ERROR.errorText))
                    is Success -> {
                        val signUpUserResponse = UserAuthResponse(
                            signUpRequest.username,
                            signUpRequest.name,
                            signUpRequest.surname,
                            tokenResult.value
                        )
                        Response(Status.CREATED)
                            .body(objectMapper.writeValueAsString(signUpUserResponse))
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
        val login = form.username
        val name = form.name
        val surname = form.surname
        val password = form.password
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
    LOGIN_ALREADY_EXISTS("Логин уже занят"),
    UNKNOWN_DATABASE_ERROR("Что-то случилось. Пожалуйста, повторите попытку позднее или обратитесь за помощью"),
    TOKEN_CREATION_ERROR("Что-то случилось. Пожалуйста, повторите попытку позднее или обратитесь за помощью"),
    NAME_IS_BLANK_OR_EMPTY("Имя не должно быть пустым"),
    NAME_IS_TOO_LONG("Максимальная длина имени - ${User.MAX_LENGTH}"),
    NAME_PATTERN_MISMATCH("Имя должно содержать только кириллические символы, пробелы и дефисы"),
    LOGIN_IS_BLANK_OR_EMPTY("Логин не должен быть пустым"),
    LOGIN_IS_TOO_LONG("Максимальная длина логина - ${User.MAX_LENGTH}"),
    LOGIN_PATTERN_MISMATCH("Логин должен содержать только латинские символы, числа и знаки"),
    SURNAME_IS_BLANK_OR_EMPTY("Фамилия не должна быть пустой"),
    SURNAME_IS_TOO_LONG("Максимальная длина фамилии - ${User.MAX_LENGTH}"),
    SURNAME_PATTERN_MISMATCH("Фамилия должна содержать только кириллические символы, пробелы и дефисы"),
    PASSWORD_IS_BLANK_OR_EMPTY("Пароль не должен быть пустым"),
}
