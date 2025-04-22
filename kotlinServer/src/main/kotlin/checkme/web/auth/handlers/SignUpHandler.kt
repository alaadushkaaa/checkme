package checkme.web.auth.handlers

import checkme.db.users.UserOperations
import checkme.domain.accounts.Role
import checkme.domain.models.User
import checkme.domain.operations.users.UserCreationError
import checkme.domain.operations.users.UserOperationHolder
import checkme.domain.tools.JWTTools
import checkme.web.auth.forms.SignUpRequest
import checkme.web.auth.forms.UserSignUpResponse
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.failureOrNull
import org.http4k.core.*
import org.http4k.lens.WebForm

class SignUpHandler(
    private val userOperations: UserOperationHolder,
    private val jwtTools: JWTTools,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        //objectMapper.writeValueAsString(task.answer))
        val objectMapper = jacksonObjectMapper()
        val signUpRequest = objectMapper.readValue<SignUpRequest>(request.bodyString())
        println(signUpRequest)
        when (val userInsertResult = tryInsert(form = signUpRequest, userOperations = userOperations)) {
            is Failure -> return Response(Status.CONFLICT).body(objectMapper.writeValueAsString(userInsertResult.reason.errorText))
            is Success -> {
                when (val tokenResult = jwtTools.createUserJwt(userInsertResult.value.id)) {
                    is Failure -> return Response(Status.INTERNAL_SERVER_ERROR).body(objectMapper.writeValueAsString(SignUpError.TOKEN_CREATION_ERROR.errorText))
                    is Success -> {
                        val signUpUserResponse = UserSignUpResponse(
                            signUpRequest.username,
                            signUpRequest.name,
                            signUpRequest.surname
                        )
                        val signUpTokenResponse = mapOf(
                            "user_data" to signUpUserResponse,
                            "token" to tokenResult.value
                        )
                        println(signUpTokenResponse)
                        return Response(Status.CREATED).body(objectMapper.writeValueAsString(signUpTokenResponse))
                    }
                }
            }
        }
    }

    private fun tryInsert(
        form: SignUpRequest,
        userOperations: UserOperationHolder,
    ): Result<User, SignUpError> {
        val login = form.username
        val name = form.name
        val surname = form.surname
        return when (
            val result = userOperations
                .createUser(name, surname, login, Role.STUDENT)
        ) {
            is Success -> Success(result.value)
            is Failure -> Failure(
                when (result.failureOrNull()) {
                    UserCreationError.LOGIN_ALREADY_EXISTS -> SignUpError.LOGIN_ALREADY_EXISTS
                    UserCreationError.INVALID_USER_DATA -> SignUpError.INVALID_USER_DATA
                    else -> SignUpError.UNKNOWN_DATABASE_ERROR
                }
            )
        }
    }
}

enum class SignUpError(val errorText: String) {
    PASSWORD_IS_BLANK_OR_EMPTY("Пароль должен быть не пустым"),
    REPEAT_PASSWORD_IS_BLANK_OR_EMPTY("Повтор пароля не может быть пустым"),
    PASSWORDS_DO_NOT_MATCH("Пароли должны совпадать"),
    LOGIN_ALREADY_EXISTS("Имя пользователя уже занято"),
    PHONE_ALREADY_EXISTS("Номер телефона уже занят"),
    EMAIL_ALREADY_EXISTS("Адрес электронной почты уже занят"),
    UNKNOWN_DATABASE_ERROR("Что-то случилось. Пожалуйста, повторите попытку позднее или обратитесь за помощью"),
    TOKEN_CREATION_ERROR("Что-то случилось. Пожалуйста, повторите попытку позднее или обратитесь за помощью"),
    INVALID_USER_DATA("Неверные данные пользователя"),
}
