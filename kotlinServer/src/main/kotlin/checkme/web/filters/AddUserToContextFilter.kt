package checkme.web.filters

import checkme.domain.models.User
import checkme.domain.operations.users.UserOperationHolder
import checkme.domain.tools.JWTTools
import dev.forkhandles.result4k.Success
import org.http4k.core.*
import org.http4k.lens.RequestContextLens
import java.util.UUID

class AddUserToContextFilter(
    private val userLens: RequestContextLens<User?>,
    private val userOperations: UserOperationHolder,
    private val jwtTools: JWTTools,
) : Filter {
    override fun invoke(next: HttpHandler): HttpHandler =
        { request: Request ->
            when (val jwt = request.header("Authentication")) {
                is String -> {
                    when (val id = jwtTools.verifyToken(jwt.substringAfter("Bearer "))) {
                        is Success -> Success(
                            next(
                                request.with(
                                    userLens of when (
                                        val result = userOperations.fetchUserById(UUID.fromString(id.value))
                                    ) {
                                        is Success -> result.value
                                        else -> null
                                    }
                                )
                            )
                        )

                        else -> Success(next(request))
                    }.value
                }

                else -> next(request)
            }
        }
}
