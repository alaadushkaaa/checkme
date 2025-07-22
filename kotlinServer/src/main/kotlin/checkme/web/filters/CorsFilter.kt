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
            // todo убрать адрес старого клиента после создания нового
            originPolicy = OriginPolicy.AnyOf(
                "http://localhost:${config.webConfig.port}", // разрешает сервер
                "http://localhost:8080", // разрешает клиент
                "http://localhost:3000" // разрешает старый клиент
            ),
            headers = listOf(
                "content-type",
                "access-control-allow-origin",
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials",
                "authorization",
                "x-custom-header",
                "authentication"
            ),
            methods = listOf(Method.GET, Method.POST, Method.PUT, Method.DELETE),
            credentials = true,
        )
    )
