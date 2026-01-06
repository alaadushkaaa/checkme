package ru.yarsu.contentPages.content.taskPage

import io.kvision.core.onClick
import io.kvision.core.onClickLaunch
import io.kvision.core.onInput
import io.kvision.form.formPanel
import io.kvision.form.getDataWithFileContent
import io.kvision.form.upload.Upload
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.html.h2
import io.kvision.html.h3
import io.kvision.panel.VPanel
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import io.kvision.types.base64Encoded
import io.kvision.types.contentType
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import org.w3c.files.File
import org.w3c.files.FilePropertyBag
import org.w3c.xhr.FormData
import ru.yarsu.contentPages.Loading
import ru.yarsu.contentPages.content.hiddenTask.TaskHiddenButton
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.serializableClasses.task.CheckId
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.task.SolutionFileList
import ru.yarsu.serializableClasses.task.TaskFormat
import kotlin.io.encoding.Base64

class TaskViewer(
    private val task: TaskFormat,
    private val serverUrl: String,
    private val routing: Routing
) : VPanel(className = "Task") {
    init {
        h2(task.name)
        if (UserInformationStorage.isAdmin()) {
            div("Решения задачи", className = "task-link").onClick {
                routing.navigate("solution-list/task/${task.id}")
            }
        }
        h3("Описание")
        div(task.description, className = "task-description", rich = true)
        h3("Ваш ответ")
        val formPanelSendSolution = formPanel<SolutionFileList>(className = "block answers-box") {
            add(
                SolutionFileList::file,
                Upload(label = task.answerFormat[0].name),
                required = true,
                requiredMessage = "Вы не прикрепили файл с решением"
            )
        }
        val buttonSend = button("Отправить", disabled = true, className = "usually-button")
        formPanelSendSolution.onInput {
            buttonSend.disabled = true
            if (formPanelSendSolution.validate()){
                buttonSend.disabled = false
            }
        }
        buttonSend.onClickLaunch {
            buttonSend.disabled = true
            val file = formPanelSendSolution.getDataWithFileContent().file[0]
            val encodedFile = file.base64Encoded
            val decodedFile = if (encodedFile != null) {
                Base64.Default.decode(encodedFile).decodeToString()
            } else {
                ""
            }
            val name = file.name
            val expansion = name.split(".").last()
            val contentType = if (expansion == "sql") "application/sql" else file.contentType
            val formData = FormData().apply {
                append("ans", value = File(
                    arrayOf(decodedFile),
                    name,
                    FilePropertyBag(type = contentType))
                )
            }
            this@TaskViewer.removeAll()
            this@TaskViewer.add(Loading("Решение отправлено. Идёт проверка..."))
            val requestInit = RequestInit()
            requestInit.method = HttpMethod.POST.name
            requestInit.headers = js("{}")
            requestInit.headers["Authentication"] = "Bearer ${UserInformationStorage.getUserInformation()?.token}"
            requestInit.body = formData
            window.fetch(serverUrl + "solution/new/${task.id}", requestInit).then { response ->
                if (response.status.toInt() == 200) {
                    response.json().then {
                        val jsonString = JSON.stringify(it)
                        val checkId = Json.Default.decodeFromString<CheckId>(jsonString)
                        routing.navigate("/solution/${checkId.checkId}")
                    }
                } else if (response.status.toInt() == 400) {
                    response.json().then {
                        val jsonString = JSON.stringify(it)
                        val responseError =
                            Json.Default.decodeFromString<ResponseError>(jsonString)
                        Toast.danger(responseError.error,
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
        if (UserInformationStorage.isAdmin()) {
            val hiddenButton = TaskHiddenButton(
                serverUrl,
                task.isActual,
                task.id
            )
            this.add(hiddenButton)
            button("Удалить задачу", className = "usually-button warning-button").onClick {
                val requestInit = RequestInit()
                requestInit.method = HttpMethod.DELETE.name
                requestInit.headers = js("{}")
                requestInit.headers["Authentication"] = "Bearer ${UserInformationStorage.getUserInformation()?.token}"
                window.fetch(serverUrl + "task/delete/${task.id}", requestInit).then { response ->
                    if (response.status.toInt() == 200) {
                        routing.navigate("/")
                    } else if (response.status.toInt() == 400) {
                        response.json().then {
                            val jsonString = JSON.stringify(it)
                            val responseError =
                                Json.Default.decodeFromString<ResponseError>(jsonString)
                            Toast.danger(
                                responseError.error,
                                ToastOptions(
                                    duration = 3000,
                                    position = ToastPosition.TOPRIGHT,
                                )
                            )
                        }
                    } else {
                        Toast.danger(
                            "Код ошибки ${response.status}: ${response.statusText}",
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
}