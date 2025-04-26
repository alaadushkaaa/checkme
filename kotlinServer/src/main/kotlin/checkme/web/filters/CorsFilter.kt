package checkme.web.filters

import checkme.config.AppConfig
import org.http4k.core.*
import org.http4k.filter.AnyOf
import org.http4k.filter.CorsPolicy
import org.http4k.filter.OriginPolicy
import org.http4k.filter.ServerFilters

fun corsFilter(config: AppConfig): Filter =
    ServerFilters.Cors(
        CorsPolicy(
            originPolicy = OriginPolicy.AnyOf(
                "http://localhost:${config.webConfig.port}", // разрешает сервер
                "http://localhost:3000" // разрешает клиент
            ),
            headers = listOf("content-type", "access-control-allow-origin", "authorization", "x-custom-header"),
            methods = listOf(Method.GET, Method.POST, Method.PUT, Method.DELETE),
            credentials = true,
        )
    )
