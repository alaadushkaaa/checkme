package ru.yarsu.contentPages.content.userListPage

import io.kvision.core.onChangeLaunch
import io.kvision.core.onClickLaunch
import io.kvision.core.onInput
import io.kvision.form.FormPanel
import io.kvision.form.formPanel
import io.kvision.form.upload.Upload
import io.kvision.form.upload.getFileWithContent
import io.kvision.html.Button
import io.kvision.html.Div
import io.kvision.html.Label
import io.kvision.html.button
import io.kvision.html.h2
import io.kvision.panel.SimplePanel
import io.kvision.panel.hPanel
import io.kvision.panel.vPanel
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import io.kvision.types.KFile
import io.kvision.types.base64Encoded
import io.kvision.types.contentType
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.events.InputEvent
import org.w3c.fetch.RequestInit
import org.w3c.files.File
import org.w3c.files.FilePropertyBag
import org.w3c.xhr.FormData
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.admin.GetSystemPasswords
import ru.yarsu.serializableClasses.signUp.FormLoadStudents
import ru.yarsu.serializableClasses.task.FormAddTask
import ru.yarsu.serializableClasses.user.UserInList
import kotlin.io.encoding.Base64

class UserList(
    serverUrl: String,
    routing: Routing
) : SimplePanel(className = "AddUserAutomatically") {
    init {
        h2("Список пользователей")
        hPanel(className = "actionsPanel") {
            vPanel(className = "actionPanel") {
                val studentsDataFile = mutableListOf<KFile>()
                val formAutomaticRegistration = formPanel<FormLoadStudents>(className = "base-form") {
                    add(Label("Автоматическая регистрация", className = "separate-form-label"))
                    val addedFileViewer = Div("Файл не выбран", className = "files-viewer")
                    add(
                        Label("Выберите csv-файл", forId = "data-file", className = "file-upload")
                    )
                    add(
                        FormLoadStudents::studentsData,
                        Upload(accept = listOf(".csv")) {
                            this.input.id = "data-file"
                            onChangeLaunch {
                                val dataFile =
                                    this@Upload.getValue()?.map { file -> this@Upload.getFileWithContent(file) }
                                        ?: emptyList()
                                studentsDataFile.clear()
                                studentsDataFile.addAll(dataFile)
                                updateFilesViewer(addedFileViewer, studentsDataFile, this@formPanel)
                                this@Upload.clearInput()
                                this@formPanel.getElement()?.dispatchEvent(InputEvent("input"))
                                this@formPanel.validate()
                            }
                        }, validatorMessage = { "" }
                    ) {
                        studentsDataFile.isNotEmpty()
                    }
                    add(
                        addedFileViewer
                    )
                    this.validate()
                    add(
                        addedFileViewer
                    )
                }

                val buttonSend = button("Загрузить", disabled = true, className = "usually-button")

                formAutomaticRegistration.onInput {
                    buttonSend.disabled = studentsDataFile.isEmpty()
                }

                buttonSend.onClickLaunch {
                    buttonSend.disabled = true
                    val formData = FormData().apply {
                        val dataFilesWithContent = if (studentsDataFile.isEmpty()) null else studentsDataFile
                        if (dataFilesWithContent != null) {
                            val csvFile = dataFilesWithContent[0]
                            val csvEncodedContent = csvFile.base64Encoded
                            val csvDecodedContent = if (csvEncodedContent != null) {
                                Base64.Default.decode(csvEncodedContent).decodeToString()
                            } else {
                                ""
                            }
                            val contentType = csvFile.contentType
                            append(
                                name = "file",
                                value = File(
                                    arrayOf(csvDecodedContent),
                                    csvFile.name,
                                    FilePropertyBag(type = contentType)
                                ),
                            )
                        }
                    }
                    val requestInit = RequestInit()
                    requestInit.method = HttpMethod.POST.name
                    requestInit.headers = js("{}")
                    requestInit.headers["Authentication"] =
                        "Bearer ${UserInformationStorage.getUserInformation()?.token}"
                    requestInit.body = formData
                    window.fetch(serverUrl + "user/automatic-registration", requestInit).then { response ->
                        if (response.status.toInt() == 200) {
                            studentsDataFile.clear()
                            formAutomaticRegistration.clearData()
                            js("window.location.reload()")
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

            vPanel(className = "actionPanel") {
                val emailsDataFile = mutableListOf<KFile>()
                val formGetSystemPasswords = formPanel<GetSystemPasswords>(className = "base-form") {
                    add(Label("Получение системных паролей", className = "separate-form-label"))
                    val addedFileViewer = Div("Файл не выбран", className = "files-viewer")
                    add(
                        Label("Выберите csv-файл", forId = "emails-file", className = "file-upload")
                    )
                    add(
                        GetSystemPasswords::studentsData,
                        Upload(accept = listOf(".csv")) {
                            this.input.id = "emails-file"
                            onChangeLaunch {
                                val dataEmailsFile =
                                    this@Upload.getValue()?.map { file -> this@Upload.getFileWithContent(file) }
                                        ?: emptyList()
                                emailsDataFile.clear()
                                emailsDataFile.addAll(dataEmailsFile)
                                updateFilesPasswordsViewer(addedFileViewer, emailsDataFile, this@formPanel)
                                this@Upload.clearInput()
                                this@formPanel.getElement()?.dispatchEvent(InputEvent("input"))
                                this@formPanel.validate()
                            }
                        }, validatorMessage = { "" }
                    ) {
                        emailsDataFile.isNotEmpty()
                    }
                    add(
                        addedFileViewer
                    )
                    this.validate()
                    add(
                        addedFileViewer
                    )
                }

                val buttonSendData = button("Скачать", disabled = true, className = "usually-button")

                formGetSystemPasswords.onInput {
                    buttonSendData.disabled = emailsDataFile.isEmpty()
                }

                buttonSendData.onClickLaunch {
                    buttonSendData.disabled = true
                    val formData = FormData().apply {
                        val dataFilesWithContent = if (emailsDataFile.isEmpty()) null else emailsDataFile
                        if (dataFilesWithContent != null) {
                            val csvFile = dataFilesWithContent[0]
                            val csvEncodedContent = csvFile.base64Encoded
                            val csvDecodedContent = if (csvEncodedContent != null) {
                                Base64.Default.decode(csvEncodedContent).decodeToString()
                            } else {
                                ""
                            }
                            val contentType = csvFile.contentType
                            append(
                                name = "file",
                                value = File(
                                    arrayOf(csvDecodedContent),
                                    csvFile.name,
                                    FilePropertyBag(type = contentType)
                                ),
                            )
                        }
                    }
                    val requestInit = RequestInit()
                    requestInit.method = HttpMethod.POST.name
                    requestInit.headers = js("{}")
                    requestInit.headers["Authentication"] =
                        "Bearer ${UserInformationStorage.getUserInformation()?.token}"
                    requestInit.body = formData
                    window.fetch(serverUrl + "admin/get-system-passwords", requestInit).then { response ->
                        if (response.status.toInt() == 200) {
                            val fileName = "system-passwords.csv"
                            response.blob().then { blob ->
                                val windowDynamic = window.asDynamic()
                                val documentDynamic = document.asDynamic()
                                val url = windowDynamic.URL.createObjectURL(blob)
                                val a = documentDynamic.createElement("a")
                                a.style.display = "none"
                                a.href = url
                                a.download = fileName
                                documentDynamic.body.appendChild(a)
                                a.click()
                                windowDynamic.URL.revokeObjectURL(url)
                                documentDynamic.body.removeChild(a)
                            }
                            emailsDataFile.clear()
                            formGetSystemPasswords.clearData()
                            buttonSendData.disabled = false
                        } else if (response.status.toInt() == 400) {
                            response.json().then {
                                val jsonString = JSON.stringify(it)
                                val responseError =
                                    Json.Default.decodeFromString<ResponseError>(jsonString)
                                Toast.danger(
                                    responseError.error,
                                    ToastOptions(
                                        duration = 8000,
                                        position = ToastPosition.TOPRIGHT,
                                    )
                                )
                            }
                            buttonSendData.disabled = false
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
        val requestInit = RequestInit()
        requestInit.method = HttpMethod.GET.name
        requestInit.headers = js("{}")
        requestInit.headers["Authentication"] = "Bearer ${UserInformationStorage.getUserInformation()?.token}"
        window.fetch(serverUrl + "user/all", requestInit).then { response ->
            if (response.status.toInt() == 200) {
                response.json().then {
                    val jsonString = JSON.stringify(it)
                    val userList = Json.decodeFromString<List<UserInList>>(jsonString)
                    this.add(UserListViewer(userList, routing))
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

    fun updateFilesViewer(filesViewer: Div, files: MutableList<KFile>, form: FormPanel<FormLoadStudents>) {
        filesViewer.removeAll()
        if (files.isEmpty()) {
            filesViewer.content = "Файл не выбран"
        } else {
            files.forEach { kFile ->
                filesViewer.content = ""
                val fileViewer = Div().apply {
                    add(Div(kFile.name))
                    add(Button("Удалить файл", className = "delete-file-button") {
                        onClick {
                            files.remove(kFile)
                            updateFilesViewer(filesViewer, files, form)
                            form.getElement()?.dispatchEvent(InputEvent("input"))
                            form.validate()
                        }
                    })
                }
                filesViewer.add(fileViewer)
            }
        }
    }

    fun updateFilesPasswordsViewer(
        filesViewer: Div,
        files: MutableList<KFile>,
        form: FormPanel<GetSystemPasswords>
    ) {
        filesViewer.removeAll()
        if (files.isEmpty()) {
            filesViewer.content = "Файл не выбран"
        } else {
            files.forEach { kFile ->
                filesViewer.content = ""
                val fileViewer = Div().apply {
                    add(Div(kFile.name))
                    add(Button("Удалить файл", className = "delete-file-button") {
                        onClick {
                            files.remove(kFile)
                            updateFilesPasswordsViewer(filesViewer, files, form)
                            form.getElement()?.dispatchEvent(InputEvent("input"))
                            form.validate()
                        }
                    })
                }
                filesViewer.add(fileViewer)
            }
        }
    }
}