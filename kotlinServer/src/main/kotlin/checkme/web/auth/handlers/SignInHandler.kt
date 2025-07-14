package checkme.web.auth.handlers

import checkme.config.AuthConfig
import checkme.domain.accounts.PasswordHasher
import checkme.domain.models.User
import checkme.domain.operations.users.UserFetchingError
import checkme.domain.operations.users.UserOperationHolder
import checkme.domain.tools.JWTTools
import checkme.web.auth.forms.SignInRequest
import checkme.web.auth.forms.UserAuthResponse
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import org.http4k.core.*

class SignInHandler(
    private val userOperations: UserOperationHolder,
    private val config: AuthConfig,
    private val jwtTools: JWTTools,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val objectMapper = jacksonObjectMapper()
        val signInRequest = objectMapper.readValue<SignInRequest>(request.bodyString())
        return when (val signInResult = checkLoginPass(signInRequest, userOperations, config)) {
            is Failure -> Response(
                Status.UNAUTHORIZED
            ).body(objectMapper.writeValueAsString(mapOf("error" to signInResult.reason.errorTest)))

            is Success -> {
                when (val tokenResult = jwtTools.createUserJwt(signInResult.value.id)) {
                    is Failure -> Response(
                        Status.UNAUTHORIZED
                    ).body(
                        objectMapper.writeValueAsString(
                            mapOf("error" to SignInError.TOKEN_CREATION_ERROR.errorTest)
                        )
                    )

                    is Success -> {
                        val signInUserResponse = UserAuthResponse(
                            signInRequest.username,
                            signInResult.value.name,
                            signInResult.value.surname,
                            tokenResult.value
                        )
//                        val signInTokenResponse = mapOf(
//                            "user_data" to signInUserResponse,
//                            "token" to tokenResult.value
//                        )
                        Response(Status.CREATED).body(objectMapper.writeValueAsString(signInUserResponse))
                    }
                }
            }
        }
    }

    private fun checkLoginPass(
        signInRequest: SignInRequest,
        userOperations: UserOperationHolder,
        config: AuthConfig,
    ): Result<User, SignInError> {
        return when (val result = userOperations.fetchUserByLogin(signInRequest.username)) {
            is Failure -> when (result.reason) {
                UserFetchingError.UNKNOWN_DATABASE_ERROR -> Failure(SignInError.UNKNOWN_DATABASE_ERROR)
                UserFetchingError.NO_SUCH_USER -> Failure(SignInError.INCORRECT_LOGIN_OR_PASS)
            }

            is Success -> if (result.value.password == PasswordHasher(config).hash(signInRequest.password)) {
                Success(result.value)
            } else {
                Failure(SignInError.INCORRECT_LOGIN_OR_PASS)
            }
        }
    }
}

enum class SignInError(val errorTest: String) {
    INCORRECT_LOGIN_OR_PASS("Неверный логин или пароль"),
    UNKNOWN_DATABASE_ERROR("Что-то случилось. Пожалуйста, повторите попытку позднее или обратитесь за помощью"),
    TOKEN_CREATION_ERROR("Что-то случилось. Пожалуйста, повторите попытку позднее или обратитесь за помощью"),
}
