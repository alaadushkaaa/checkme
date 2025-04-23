@file:Suppress("detekt:SwallowedException")

package checkme.domain.tools

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.ibm.icu.util.Calendar
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success

class JWTTools(
    private val secret: String,
    private val issue: String,
) {
    private val algorithm: Algorithm = Algorithm.HMAC512(secret)
//    private val verifier: JWTVerifier =
//        JWT.require(algorithm)
//            .withIssuer(issue)
//            .build()

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
}

enum class TokenError {
    EXPIRED_TOKEN,
    DECODING_ERROR,
    CREATION_ERROR,
}
