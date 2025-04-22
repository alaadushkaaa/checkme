package checkme.web.context

import checkme.config.WebConfig
import checkme.domain.models.User
import org.http4k.core.*
import org.http4k.lens.RequestContextKey
import org.http4k.lens.RequestContextLens

class ContextTools (
    config: WebConfig,
) {
    val appContexts = RequestContexts()
    val userLens: RequestContextLens<User?> =
        RequestContextKey
            .optional(appContexts, "user")
}