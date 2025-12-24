package ru.yarsu.contentPages.content.taskPage

import io.kvision.html.Div
import io.kvision.panel.SimplePanel
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.task.TaskFormat
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class Task(
    @OptIn(ExperimentalUuidApi::class)
    taskId: Uuid?,
    serverUrl: String,
    private val routing: Routing
) : SimplePanel() {
    init {
        val requestInit = RequestInit()
        requestInit.method = HttpMethod.GET.name
        requestInit.headers = js("{}")
        requestInit.headers["Authentication"] = "Bearer ${UserInformationStorage.getUserInformation()?.token}"
        @OptIn(ExperimentalUuidApi::class)
        window.fetch(serverUrl + "task/$taskId", requestInit).then { response ->
            if (response.status.toInt() == 200) {
                response.json().then {
                    val jsonString = JSON.stringify(it)
                    val task = Json.Default.decodeFromString<TaskFormat>(jsonString)
                    this.add(TaskViewer(task, serverUrl, routing))
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