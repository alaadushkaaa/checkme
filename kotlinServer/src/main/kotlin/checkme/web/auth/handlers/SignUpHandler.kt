package checkme.web.auth.handlers

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

class SignUpHandler(
    private val userOperations: UserOperationHolder,
    private val jwtTools: JWTTools,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val signUpRequest = objectMapper.readValue<SignUpRequest>(request.bodyString())
        println(signUpRequest)
        return when (val userInsertResult = tryInsert(form = signUpRequest, userOperations = userOperations)) {
            is Failure -> Response(Status.CONFLICT)
                .body(objectMapper.writeValueAsString(userInsertResult.reason.errorText))
            is Success -> {
                when (val tokenResult = jwtTools.createUserJwt(userInsertResult.value.id)) {
                    is Failure -> Response(
                        Status.INTERNAL_SERVER_ERROR
                    ).body(objectMapper.writeValueAsString(SignUpError.TOKEN_CREATION_ERROR.errorText))
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
                        Response(Status.CREATED).body(objectMapper.writeValueAsString(signUpTokenResponse))
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
        val password = form.password
        return when (
            val result = userOperations
                .createUser(login, name, surname, password, Role.STUDENT)
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
    LOGIN_ALREADY_EXISTS("Имя пользователя уже занято"),
    UNKNOWN_DATABASE_ERROR("Что-то случилось. Пожалуйста, повторите попытку позднее или обратитесь за помощью"),
    TOKEN_CREATION_ERROR("Что-то случилось. Пожалуйста, повторите попытку позднее или обратитесь за помощью"),
    INVALID_USER_DATA("Неверные данные пользователя"),
}
