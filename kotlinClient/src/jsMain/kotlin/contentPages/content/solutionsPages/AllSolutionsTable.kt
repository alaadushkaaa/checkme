package ru.yarsu.contentPages.content.solutionsPages

import io.kvision.html.Div
import io.kvision.html.button
import io.kvision.html.h2
import io.kvision.panel.SimplePanel
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import kotlinx.browser.window
import org.w3c.fetch.RequestInit
import ru.yarsu.localStorage.UserInformationStorage
import kotlinx.serialization.json.Json
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.solution.SolutionsTable

class AllSolutionsTable(
    serverUrl: String,
    private val routing: Routing
) : SimplePanel() {
    init {
        h2("Все решения")
        button("Список", className = "usually-button").onClick {
            routing.navigate("/solution-list/1")
        }
        val requestInit = RequestInit()
        requestInit.method = HttpMethod.GET.name
        requestInit.headers = js("{}")
        requestInit.headers["Authentication"] = "Bearer ${UserInformationStorage.getUserInformation()?.token}"
        window.fetch(serverUrl + "solution/solutions_table", requestInit).then { response ->
            if (response.status.toInt() == 200) {
                response.json().then {
                    val jsonString = JSON.stringify(it)
                    val solutions = Json.Default.decodeFromString<SolutionsTable>(jsonString)
                    this.add(AllSolutionsTableViewer(routing, solutions))
                }
            } else if (response.status.toInt() == 400) {
                response.json().then {
                    val jsonString = JSON.stringify(it)
                    val responseError = Json.Default.decodeFromString<ResponseError>(jsonString)
                    this.add(Div(responseError.error, className = "error-message"))
                }
            } else {
                this.add(Div("Код ошибки ${response.status}: ${response.statusText}", className = "error-message"))
            }
        }
    }
}