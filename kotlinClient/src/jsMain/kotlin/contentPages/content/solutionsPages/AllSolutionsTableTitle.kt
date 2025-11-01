package ru.yarsu.contentPages.content.solutionsPages

import io.kvision.html.Div
import io.kvision.panel.SimplePanel
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.solution.SolutionInAdminListsFormat
import ru.yarsu.serializableClasses.task.TaskFormatForList

class AllSolutionsTableTitle(
    serverUrl: String,
    private val routing: Routing,
    solutionList: List<SolutionInAdminListsFormat>,
): SimplePanel() {
    init {
        val requestInit = RequestInit()
        requestInit.method = HttpMethod.GET.name
        requestInit.headers = js("{}")
        requestInit.headers["Authentication"] = "Bearer ${UserInformationStorage.getUserInformation()?.token}"
        window.fetch(serverUrl + "task/all", requestInit).then { response ->
            if (response.status.toInt() == 200) {
                response.json().then {
                    val jsonString = JSON.stringify(it)
                    val taskList = Json.decodeFromString<List<TaskFormatForList>>(jsonString)
                    this.add(AllSolutionsTableViewer(routing, taskList, solutionList))
                }
            } else if (response.status.toInt() == 400) {
                response.json().then {
                    val jsonString = JSON.stringify(it)
                    val responseError =
                        Json.Default.decodeFromString<ResponseError>(jsonString)
                    this.add(Div(responseError.error, className = "error-message"))
                }
            } else {
                this.add(Div("Код ошибки ${response.status}: ${response.statusText}", className = "error-message"))
            }
        }
    }
}