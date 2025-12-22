package ru.yarsu.contentPages.content.addBundlePage

import io.kvision.core.Container
import io.kvision.core.onClick
import io.kvision.form.check.CheckBox
import io.kvision.html.Button
import io.kvision.html.Div
import io.kvision.html.h2
import io.kvision.panel.SimplePanel
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.bundle.TaskFormatWithOrder
import ru.yarsu.serializableClasses.task.TaskFormatForList

class ChangeBundleTasksOrder(
    bundleId: String,
    serverUrl: String,
    private val routing: Routing,
) : SimplePanel() {
    init {
        h2("Укажите порядок заданий в группе")
        val requestInit = RequestInit()
        requestInit.method = HttpMethod.GET.name
        requestInit.headers = js("{}")
        requestInit.headers["Authentication"] = "Bearer ${UserInformationStorage.getUserInformation()?.token}"
        window.fetch(serverUrl + "bundle/tasks/$bundleId", requestInit).then { response ->
            if (response.status.toInt() == 200) {
                response.text().then { jsonString ->
                    println(jsonString)
                    println("*/*/*/*/*/*/*")
                    val taskList = Json.decodeFromString<List<TaskFormatWithOrder>>(jsonString)
                    if (taskList.isEmpty()) {
                        this.add(Div("Задачи не найдены"))
                    } else {
                        val tasksContainer = Div(className = "task-selection-container")
                        h2("Ура!")
                        add(tasksContainer)

                        taskList.forEach { task ->
                            createTaskElement(task, tasksContainer)
                        }
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

    private fun createTaskElement(
        task: TaskFormatWithOrder,
        container: Container
    ) {
        val taskDiv = Div(className = "task-item").apply {
            val checkbox =
                CheckBox(value = false,
                    label = "${task.task.name} (${task.task.description.replace("(<([^>]+)>)".toRegex(), "")
                        .slice(0..DESCRIPTION_SIZE - task.task.name.length)}...)"
                )
            add(checkbox)
        }
        container.add(taskDiv)
    }

}