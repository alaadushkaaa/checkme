package ru.yarsu.contentPages.content.taskPage

import io.kvision.core.onChangeLaunch
import io.kvision.core.onClick
import io.kvision.core.onClickLaunch
import io.kvision.core.onInput
import io.kvision.form.FormPanel
import io.kvision.form.formPanel
import io.kvision.form.getDataWithFileContent
import io.kvision.form.upload.Upload
import io.kvision.form.upload.getFileWithContent
import io.kvision.html.Button
import io.kvision.html.Div
import io.kvision.html.Label
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.html.h2
import io.kvision.html.h3
import io.kvision.html.h4
import io.kvision.panel.VPanel
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import io.kvision.types.KFile
import io.kvision.types.base64Encoded
import io.kvision.types.contentType
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.w3c.dom.events.InputEvent
import org.w3c.fetch.RequestInit
import org.w3c.files.File
import org.w3c.files.FilePropertyBag
import org.w3c.xhr.FormData
import ru.yarsu.contentPages.Loading
import ru.yarsu.contentPages.content.hiddenTask.TaskHiddenButton
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.serializableClasses.task.CheckId
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.task.FormAddTask
import ru.yarsu.serializableClasses.task.SolutionFileList
import ru.yarsu.serializableClasses.task.TaskFormat
import kotlin.io.encoding.Base64

class TaskViewer(
    private val task: TaskFormat,
    private val serverUrl: String,
    private val routing: Routing
) : VPanel(className = "Task") {

    private var solutionFile : KFile? = null

    init {
        h2(task.name)
        if (UserInformationStorage.isAdmin()) {
            div("Решения задачи", className = "task-link").onClick {
                routing.navigate("solution-list/task/${task.id}")
            }
        }
        val bestResult = if (task.bestScore == -1) {
            "нет результата"
        } else {
            task.bestScore.toString()
        }
        div("Ваш лучший результат: $bestResult", className = "best-solution")
        h3("Описание")
        div(task.description, className = "task-description", rich = true)
        div("Предоставьте ответ в виде файла с расширением .sql", className = "solution-label")
        val formPanelSendSolution = formPanel<SolutionFileList>(className = "answer") {
            val addedFileViewer = Div("Файл не выбран", className = "file-viewer")
            add(
                Label("Выберите файл c решением", forId = "input-solution-file", className = "file-upload")
            )
            add(
                SolutionFileList::file,
                Upload(accept = listOf(".sql"), multiple = false) {
                    this.input.id = "input-solution-file"
                    onChangeLaunch {
                        solutionFile = this@Upload.getValue()?.map { file -> this@Upload.getFileWithContent(file) }[0]
                        updateFileViewer(addedFileViewer, solutionFile, this@formPanel)
                        this@Upload.clearInput()
                        this@formPanel.getElement()?.dispatchEvent(InputEvent("input"))
                        this@formPanel.validate()
                    }
                },
                validatorMessage = { "" }
            ) {
                solutionFile != null
            }
            this.validate()
            add(
                addedFileViewer
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
            val file = solutionFile ?: KFile("", 0)
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

    fun updateFileViewer(fileViewer: Div, file: KFile?, form: FormPanel<SolutionFileList>) {
        fileViewer.removeAll()
        if (file == null) {
            fileViewer.content = "Файл не выбран"
        } else if (file.name.split(".").last() != "sql") {
            Toast.danger("Файл недопустимого формата!")
            fileViewer.content = "Файл не выбран"
            solutionFile = null
        } else {
            fileViewer.content = ""
            val file = Div().apply {
                add(Div(file.name))
                add(Button("Удалить файл", className = "delete-file-button") {
                    onClick {
                        updateFileViewer(fileViewer, null, form)
                        solutionFile = null
                        form.getElement()?.dispatchEvent(InputEvent("input"))
                        form.validate()
                    }
                })
            }
            fileViewer.add(file)
        }
    }
}