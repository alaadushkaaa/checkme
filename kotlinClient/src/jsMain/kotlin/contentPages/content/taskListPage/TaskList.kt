package ru.yarsu.contentPages.content.taskListPage

import io.kvision.html.Div
import io.kvision.html.button
import io.kvision.html.h2
import io.kvision.panel.SimplePanel
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.task.TaskIdName

class TaskList(
    serverUrl : String,
    private val routing: Routing,
) : SimplePanel(){
    init {
        h2("Список задач")
        if (UserInformationStorage.isAdmin()) {
            button(
                "Cоздать задачу",
                className = "usually-button"
            ).onClick { routing.navigate("/add-task") }
        }
        val requestInit = RequestInit()
        requestInit.method = HttpMethod.GET.name
        requestInit.headers = js("{}")
        requestInit.headers["Authentication"] = "Bearer ${UserInformationStorage.getUserInformation()?.token}"
        window.fetch(serverUrl + "task/all", requestInit).then { response ->
            if (response.status.toInt() == 200) {
                response.json().then {
                    val jsonString = JSON.stringify(it)
                    val taskList = Json.decodeFromString<List<TaskIdName>>(jsonString)
                    if (taskList.isEmpty()){
                        this.add(Div("Задачи не найдены"))
                    } else {
                        this.add(TaskListViewer(routing, taskList))
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