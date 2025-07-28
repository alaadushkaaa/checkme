@file:Suppress("detekt:SwallowedException")

package checkme.domain.tools

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.ibm.icu.util.Calendar
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success

class JWTTools(
    private val secret: String,
    private val issue: String,
) {
    private val algorithm: Algorithm = Algorithm.HMAC512(secret)
    private val verifier: JWTVerifier =
        JWT.require(algorithm)
            .withIssuer(issue)
            .build()

    fun createUserJwt(
        userId: Int,
        lifeTimeInDays: Int = 7,
    ): Result<String, TokenError> =
        Calendar.getInstance()
            .apply {
                add(Calendar.DAY_OF_MONTH, lifeTimeInDays)
            }.let { expirationDate ->
                try {
                    Success(
                        JWT.create()
                            .withIssuer(issue)
                            .withExpiresAt(expirationDate.time)
                            .withSubject(userId.toString())
                            .sign(algorithm)
                    )
                } catch (exception: IllegalArgumentException) {
                    Failure(TokenError.CREATION_ERROR)
                }
            }

    fun verifyToken(token: String) =
        try {
            Success(verifier.verify(token).subject)
        } catch (exception: TokenExpiredException) {
            Failure(TokenError.EXPIRED_TOKEN)
        } catch (exception: JWTDecodeException) {
            Failure(TokenError.DECODING_ERROR)
        }
}

enum class TokenError {
    EXPIRED_TOKEN,
    DECODING_ERROR,
    CREATION_ERROR,
}
