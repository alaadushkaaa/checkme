package ru.yarsu.contentPages.content

import io.kvision.rest.HttpMethod
import org.w3c.fetch.RequestInit
import ru.yarsu.localStorage.UserInformationStorage

internal fun createRequestHeaders(
    httpMethod : HttpMethod,
) : RequestInit {
    val requestInit = RequestInit()
    requestInit.method = httpMethod.name
    requestInit.headers = js("{}")
    requestInit.headers["Authentication"] = "Bearer ${UserInformationStorage.getUserInformation()?.token}"
    return requestInit
}