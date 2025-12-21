package ru.yarsu.contentPages.content.addBundlePage

import io.kvision.core.Container
import io.kvision.core.onClick
import io.kvision.form.check.CheckBox
import io.kvision.html.*
import io.kvision.panel.SimplePanel
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.signIn.RequestSignIn
import ru.yarsu.serializableClasses.task.TaskFormatForList

const val DESCRIPTION_SIZE = 150
class SelectBundleTasksPage(
    bundleId: String,
    serverUrl: String,
    private val routing: Routing,
) : SimplePanel() {
    private val selectedTasks = mutableSetOf<String>()
    init {
        h2("Выбор задач")
        val saveButton = button(
            "Далее",
            className = "usually-button"
        ).apply {
            disabled = true
            onClick {
                if (selectedTasks.isNotEmpty()) {
                    val requestInit = RequestInit()
                    requestInit.method = HttpMethod.POST.name
                    requestInit.headers = js("{}")
                    requestInit.headers["Content-Type"] = "application/json"
                    requestInit.headers["Authentication"] = "Bearer ${UserInformationStorage.getUserInformation()?.token}"
                    requestInit.body = Json.Default.encodeToString(
                        selectedTasks
                    )
                    window.fetch(serverUrl + "bundle/select-order/$bundleId", requestInit).then { response ->
                        if (response.status.toInt() == 200) {
                            response.json().then {
                                val jsonString = JSON.stringify(it)
                                UserInformationStorage.addUserInformation(jsonString)
                                routing.navigate("bundle/select-order/$bundleId")
                            }
                        } else if (response.status.toInt() == 401) {
                            response.json().then {
                                val jsonString = JSON.stringify(it)
                                val responseUnauthorized =
                                    Json.Default.decodeFromString<ResponseError>(jsonString)
                                Toast.danger(responseUnauthorized.error,
                                    ToastOptions(
                                        duration = 3000,
                                        position = ToastPosition.TOPRIGHT,
                                    )
                                )
                            }
                        } else {
                            Toast.danger("Код ошибки ${response.status}: ${response.statusText}",
                                ToastOptions(
                                    duration = 5000,
                                    position = ToastPosition.TOPRIGHT,
                                )
                            )
                        }
                    }
                }
            }
        }
        add(saveButton)
        val requestInit = RequestInit()
        requestInit.method = HttpMethod.GET.name
        requestInit.headers = js("{}")
        requestInit.headers["Authentication"] = "Bearer ${UserInformationStorage.getUserInformation()?.token}"
        window.fetch(serverUrl + "task/all", requestInit).then { response ->
            if (response.status.toInt() == 200) {
                response.json().then {
                    val jsonString = JSON.stringify(it)
                    console.log("Полученный JSON:", jsonString)
                    val taskList = Json.decodeFromString<List<TaskFormatForList>>(jsonString)
                    if (taskList.isEmpty()) {
                        this.add(Div("Задачи не найдены"))
                    } else {
                        val tasksContainer = Div(className = "task-selection-container")
                        add(tasksContainer)

                        taskList.forEach { task ->
                            createTaskElement(task, tasksContainer, saveButton)
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
        task: TaskFormatForList,
        container: Container,
        nextButton: Button
    ) {
        val taskDiv = Div(className = "task-item").apply {
            val checkbox =
                CheckBox(value = false,
                    label = "${task.name} (${task.description.replace("(<([^>]+)>)".toRegex(), "")
                        .slice(0..DESCRIPTION_SIZE - task.name.length)}...)"
                )
            add(checkbox)
            onClick {
                checkbox.value = !checkbox.value
                changeTaskSelection(
                    task.id.toString(),
                    checkbox.value,
                    nextButton
                )
            }
        }
        container.add(taskDiv)
    }

    private fun changeTaskSelection(taskId: String, isSelected: Boolean, nextButton: Button) {
        if (isSelected) {
            selectedTasks.add(taskId)
        } else {
            selectedTasks.remove(taskId)
        }
        nextButton.disabled = selectedTasks.isEmpty()
    }
}