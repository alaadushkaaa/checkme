package checkme.web.auth.handlers

import checkme.config.AuthConfig
import checkme.domain.accounts.PasswordHasher
import checkme.domain.models.User
import checkme.domain.operations.users.UserFetchingError
import checkme.domain.operations.users.UserOperationHolder
import checkme.domain.tools.JWTTools
import checkme.web.auth.forms.SignInRequest
import checkme.web.auth.forms.UserAuthResponse
import checkme.web.commonExtensions.sendStatusCreated
import checkme.web.commonExtensions.sendStatusUnauthorized
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
            is Failure -> objectMapper.sendStatusUnauthorized(signInResult.reason.errorTest)

            is Success -> {
                when (val tokenResult = jwtTools.createUserJwt(signInResult.value.id)) {
                    is Failure -> objectMapper.sendStatusUnauthorized(SignInError.TOKEN_CREATION_ERROR.errorTest)

                    is Success -> {
                        val signInUserResponse = UserAuthResponse(
                            signInRequest.username,
                            signInResult.value.name,
                            signInResult.value.surname,
                            signInResult.value.role.name,
                            tokenResult.value
                        )
                        objectMapper.sendStatusCreated(signInUserResponse)
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
        return when (val result = userOperations.fetchUserByLogin(signInRequest.username.trim())) {
            is Failure -> when (result.reason) {
                UserFetchingError.UNKNOWN_DATABASE_ERROR -> Failure(SignInError.UNKNOWN_DATABASE_ERROR)
                UserFetchingError.NO_SUCH_USER -> Failure(SignInError.INCORRECT_LOGIN_OR_PASS)
            }

            is Success -> if (result.value.password == PasswordHasher(config).hash(signInRequest.password.trim())) {
                Success(result.value)
            } else {
                Failure(SignInError.INCORRECT_LOGIN_OR_PASS)
            }
        }
    }
}

enum class SignInError(val errorTest: String) {
    INCORRECT_LOGIN_OR_PASS("Invalid login or password"),
    UNKNOWN_DATABASE_ERROR("Something happened. Please try again later or ask for help"),
    TOKEN_CREATION_ERROR("Something happened with token. Please try again later or ask for help"),
}
