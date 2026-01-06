package ru.yarsu.contentPages.content.solutionsPages

import io.kvision.html.Div
import io.kvision.html.H2
import io.kvision.panel.SimplePanel
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.solution.TaskOrUserSolutionsFormat
import kotlin.uuid.Uuid

class TaskOrUserSolutions(
    taskOrUserId: Uuid?,
    private val taskOrUser: String,
    serverUrl: String,
    private val routing: Routing
) : SimplePanel() {
    init {
        val title = H2("Решения")
        add(title)
        val requestInit = RequestInit()
        requestInit.method = HttpMethod.GET.name
        requestInit.headers = js("{}")
        requestInit.headers["Authentication"] = "Bearer ${UserInformationStorage.getUserInformation()?.token}"
        window.fetch(serverUrl + "solution/$taskOrUser/$taskOrUserId", requestInit).then { response ->
            if (response.status.toInt() == 200) {
                response.json().then {
                    val jsonString = JSON.stringify(it)
                    console.log(jsonString)
                    val taskOrUserSolutions = Json.Default.decodeFromString<TaskOrUserSolutionsFormat>(jsonString)
                    if (taskOrUser == "user"){
                        title.content = "Решения пользователя: ${taskOrUserSolutions.name} ${taskOrUserSolutions.surname}"
                    } else {
                        title.content = "Решения задачи: ${taskOrUserSolutions.name}"
                    }
                    if (taskOrUserSolutions.solutions.isEmpty()){
                        add(Div("Решения не найдены"))
                    } else {
                        add(AllSolutionsViewer(taskOrUserSolutions.solutions, routing))
                    }
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