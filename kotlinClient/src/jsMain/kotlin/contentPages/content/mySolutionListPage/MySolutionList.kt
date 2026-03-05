package ru.yarsu.contentPages.content.mySolutionListPage

import io.kvision.html.Div
import io.kvision.html.h2
import io.kvision.panel.SimplePanel
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import ru.yarsu.contentPages.content.createRequestHeaders
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.solution.SolutionInListFormat

class MySolutionList(
    serverUrl: String,
    private val routing: Routing
) : SimplePanel() {
    init {
        h2("Решения")
        val requestInit = createRequestHeaders(HttpMethod.GET)
        window.fetch(serverUrl + "solution/me", requestInit).then { response ->
            when (response.status.toInt()) {
                200 -> response.json().then {
                    val jsonString = JSON.stringify(it)
                    val solutionList = Json.decodeFromString<List<SolutionInListFormat>>(jsonString)
                    if (solutionList.isEmpty()) {
                        this.add(Div("Решения не найдены"))
                    } else {
                        this.add(MySolutionListViewer(solutionList, routing))
                    }
                }

                400 -> response.json().then {
                    val jsonString = JSON.stringify(it)
                    val responseError =
                        Json.Default.decodeFromString<ResponseError>(jsonString)
                    this.add(Div(responseError.error, className = "error-message"))
                }

                else -> this.add(
                    Div(
                        "Код ошибки ${response.status}: ${response.statusText}",
                        className = "error-message"
                    )
                )
            }
        }
    }
}