package ru.yarsu.contentPages.content.addTaskPage

import io.kvision.core.onChange
import io.kvision.core.onChangeLaunch
import io.kvision.core.onClickLaunch
import io.kvision.core.onInput
import io.kvision.form.FormPanel
import io.kvision.form.check.RadioGroup
import io.kvision.form.form
import io.kvision.form.formPanel
import io.kvision.form.select.Select
import io.kvision.form.select.select
import io.kvision.form.text.RichText
import io.kvision.form.text.Text
import io.kvision.form.text.TextArea
import io.kvision.form.upload.Upload
import io.kvision.form.upload.getFileWithContent
import io.kvision.html.Button
import io.kvision.html.Div
import io.kvision.html.Label
import io.kvision.html.button
import io.kvision.html.h2
import io.kvision.html.label
import io.kvision.panel.VPanel
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
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.w3c.dom.events.InputEvent
import org.w3c.dom.events.KeyboardEvent
import org.w3c.fetch.RequestInit
import org.w3c.files.File
import org.w3c.files.FilePropertyBag
import org.w3c.xhr.FormData
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.serializableClasses.task.AnswerFormat
import ru.yarsu.serializableClasses.task.Criterion
import ru.yarsu.serializableClasses.task.FormAddTask
import ru.yarsu.serializableClasses.task.FormAddTaskFileSelection
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.task.TaskId
import kotlin.io.encoding.Base64

class AddTask(
    private val serverUrl: String,
    private val routing: Routing
) : VPanel(className = "TaskAdd"){
    init {
        h2("Создание задачи")
        val fileList = mutableListOf<KFile>()
        val scriptFile = mutableListOf<KFile>()
        val formPanelAddTask = formPanel<FormAddTask>(className = "base-form") {
            add(Label("Название", className = "separate-form-label"))
            add(
                FormAddTask::name,
                Text(),
                required = true,
                requiredMessage = ""
            )
            add(Label("Описание", className = "separate-form-label"))
            add(
                FormAddTask::description,
                RichText(),
                required = true,
                requiredMessage = ""
            )
            add(Label("JSON с критериями задачи", className = "separate-form-label"))
            val textArea = TextArea()
            add(
                Label("Выберите JSON файл", forId = "input-file-0", className = "file-upload")
            )
            add(
                Upload(accept = listOf(".json")) {
                    this.input.id = "input-file-0"
                    onChangeLaunch {
                        val file = this@Upload.getValue()?.map { file -> this@Upload.getFileWithContent(file) }
                        if (file != null){
                            val encodedContent = file[0].base64Encoded
                            textArea.value = if (encodedContent != null) {
                                Base64.Default.decode(encodedContent).decodeToString()
                            } else {
                                ""
                            }
                            this@formPanel.getElement()?.dispatchEvent(InputEvent("input"))
                            this@Upload.clearInput()
                        }
                    }
                }
            )
            add(
                FormAddTask::criterion,
                textArea,
                required = true,
                requiredMessage = "",
                validatorMessage = { "Некорректный Json" }
            ) {
                val criterion =
                    try {
                        Json.Default.decodeFromString<Map<String, Criterion>>(it.getValue().toString())
                    } catch (_: SerializationException) {
                        null
                    } catch (_: IllegalArgumentException) {
                        null
                    }
                criterion != null
            }
            add(Label("Вопрос", className = "separate-form-label"))
            add(
                FormAddTask::answer,
                TextArea(),
                required = true,
                requiredMessage = "",
            )
            add(Label("Формат ответа", className = "separate-form-label"))
            add(
                FormAddTask::format,
                RadioGroup(
                    listOf(
//                        "text" to "Текст", //если будет нужен функционал с текстовым ответом,
//                        далее в клиенте пока не реализовано
                        "file" to "Файл")
                ),
                required = true,
                requiredMessage = ""
            )
            add(Label("Скрипт", className = "separate-form-label"))
            val addedScriptFileViewer = Div("Файл не выбран", className = "files-viewer")
            add(
                Label("Выберите файл", forId = "input-file-1", className = "file-upload")
            )
            add(
                FormAddTask::script,
                Upload(accept = listOf(".sql")) {
                    this.input.id = "input-file-1"
                    onChangeLaunch {
                        val scriptListFile = this@Upload.getValue()?.map { file -> this@Upload.getFileWithContent(file) } ?: emptyList()
                        scriptFile.clear()
                        scriptFile.addAll(scriptListFile)
                        updateFileViewer(addedScriptFileViewer, scriptFile, this@formPanel)
                        this@Upload.clearInput()
                        this@formPanel.getElement()?.dispatchEvent(InputEvent("input"))
                        this@formPanel.validate()
                    }
                }
            )
            add(
                addedScriptFileViewer
            )
            add(Label("Файлы тестов", className = "separate-form-label"))
            val addedFilesViewer = Div("Файлы не выбраны", className = "files-viewer")
            add(
                Label("Выберите файлы тестов", forId = "input-file-2", className = "file-upload")
            )
            add(
                FormAddTask::files,
                Upload(multiple = true) {
                    this.input.id = "input-file-2"
                    onChangeLaunch {
                        val files = this@Upload.getValue()?.map { file -> this@Upload.getFileWithContent(file) } ?: emptyList()
                        fileList.addAll(files)
                        updateFilesViewer(addedFilesViewer, fileList, this@formPanel)
                        this@Upload.clearInput()
                        this@formPanel.getElement()?.dispatchEvent(InputEvent("input"))
                        this@formPanel.validate()
                    }
                },
                validatorMessage = { "" }
            ) {
                fileList.isNotEmpty()
            }
            add(
                addedFilesViewer
            )
            this.validate()
        }
        val formPanelFileSelection = formPanel<FormAddTaskFileSelection>(className = "criterion-select")
        val formFileSelection: FormPanel<Map<String, Any?>> = form(className = "criterion-select")
        val buttonSend = button("Отправить", disabled = true, className = "usually-button")
        val fileSelectionData = mutableMapOf<String, Select>()
        formPanelAddTask.onInput {
            formPanelFileSelection.removeAll()
            formFileSelection.removeAll()
            fileSelectionData.clear()
            buttonSend.disabled = true
            if (formPanelAddTask.validate()) {
                val nameFiles = fileList.map { it.name to it.name }
                val criterionString = formPanelAddTask.getData().criterion
                val criterion = Json.Default.decodeFromString<Map<String, Criterion>>(criterionString)
                fileSelectionData["beforeEach"] = Select(
                    options = nameFiles,
                    label = "Выполнять перед каждым тестом:"
                ).apply { formPanelFileSelection.add(FormAddTaskFileSelection::beforeEach,this) }
                fileSelectionData["afterEach"] = Select(
                    options = nameFiles,
                    label = "Выполнять после каждого теста:"
                ).apply { formPanelFileSelection.add(FormAddTaskFileSelection::afterEach, this) }
                fileSelectionData["beforeAll"] = Select(
                    options = nameFiles,
                    label = "Выполнить перед тестами:"
                ).apply { formPanelFileSelection.add(FormAddTaskFileSelection::beforeAll, this) }
                fileSelectionData["afterAll"] = Select(
                    options = nameFiles,
                    label = "Выполнять после тестов:"
                ).apply { formPanelFileSelection.add(FormAddTaskFileSelection::afterAll, this) }
                formFileSelection.form {
                    val fields = mutableMapOf<String, Select>()
                    label("Соотнесите тест с запускаемым файлом", className = "special-label")
                    for (nameTest in criterion.keys) {
                        select(
                            nameFiles,
                            label = nameTest
                        ).apply {
                            bind(nameTest, required = true)
                            fields.put(nameTest, this)
                            onChange {
                                buttonSend.disabled = !fields.values.all { !it.value.isNullOrEmpty() }
                            }
                        }
                    }
                    fileSelectionData.putAll(fields)
                }
            }
        }
        buttonSend.onClickLaunch {
            val criterionString = formPanelAddTask.getData().criterion
            val criterion = Json.Default.decodeFromString<Map<String, Criterion>>(criterionString)
            val newCriterion : Map<String, Criterion> = criterion.mapValues { (key, value) ->
                val testFile = fileSelectionData[key]?.value
                if (testFile != null) {
                    Criterion(
                        value.description,
                        value.score,
                        testFile,
                        value.message
                    )
                } else {
                    value
                }
            }
            val answerFormat = listOf(
                AnswerFormat(
                    formPanelAddTask.getData().answer,
                    formPanelAddTask.getData().format
                )
            )
            val beforeEach = fileSelectionData["beforeEach"]?.value
            val afterEach = fileSelectionData["afterEach"]?.value
            val beforeAll = fileSelectionData["beforeAll"]?.value
            val afterAll = fileSelectionData["afterAll"]?.value
            val filesWithContent = fileList
            val scriptFileWithContent = if (scriptFile.isEmpty()) null else scriptFile[0]
            val formData = FormData().apply {
                append("name", formPanelAddTask.getData().name)
                append("description", formPanelAddTask.getData().description)
                append("criterions", Json.Default.encodeToString(newCriterion))
                append("answerFormat", Json.Default.encodeToString(answerFormat))
                if (beforeEach != null){
                    append("beforeEach", beforeEach)
                }
                if (afterEach != null){
                    append("afterEach", afterEach)
                }
                if (beforeAll != null){
                    append("beforeAll", beforeAll)
                }
                if (afterAll != null){
                    append("afterAll", afterAll)
                }
                if (scriptFileWithContent != null) {
                    val scriptEncodedContent = scriptFileWithContent.base64Encoded
                    val scriptDecodedContent = if (scriptEncodedContent != null) {
                        Base64.Default.decode(scriptEncodedContent).decodeToString()
                    } else {
                        ""
                    }
                    val scriptContentType = scriptFileWithContent.contentType
                    append(
                        name = "script",
                        value = File(
                            arrayOf(scriptDecodedContent),
                            scriptFileWithContent.name,
                            FilePropertyBag(type = scriptContentType)
                        )
                    )
                }
                filesWithContent.forEach { kFile ->
                    val encodedContent = kFile.base64Encoded
                    val decodedContent = if (encodedContent != null) {
                        Base64.Default.decode(encodedContent).decodeToString()
                    } else {
                        ""
                    }
                    val contentType = kFile.contentType
                    append(
                        name = "file",
                        value = File(
                            arrayOf(decodedContent),
                            kFile.name,
                            FilePropertyBag(type = contentType)
                        ),
                    )
                }
            }
            val requestInit = RequestInit()
            requestInit.method = HttpMethod.POST.name
            requestInit.headers = js("{}")
            requestInit.headers["Authentication"] = "Bearer ${UserInformationStorage.getUserInformation()?.token}"
            requestInit.body = formData
            window.fetch(serverUrl + "task/new", requestInit).then { response ->
                if (response.status.toInt() == 200) {
                    response.json().then {
                        val jsonString = JSON.stringify(it)
                        val taskId = Json.Default.decodeFromString<TaskId>(jsonString)
                        routing.navigate("/task/${taskId.taskId}")
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
        document.addEventListener("keydown", {event ->
            if (event is KeyboardEvent && event.keyCode == 13) {
                if (document.activeElement == document.body) {
                    buttonSend.getElement()?.click()
                }
            }
        })
    }

    fun updateFileViewer(fileViewer: Div, fileList: MutableList<KFile>, form: FormPanel<FormAddTask>) {
        fileViewer.removeAll()
        if (fileList.isEmpty()) {
            fileViewer.content = "Файл не выбран"
        } else {
            fileViewer.content = ""
            val file = Div().apply {
                add(Div(fileList[0].name))
                add(Button("Удалить файл", className = "delete-file-button") {
                    onClick {
                        fileList.clear()
                        updateFileViewer(fileViewer, fileList, form)
                        form.getElement()?.dispatchEvent(InputEvent("input"))
                        form.validate()
                    }
                })
            }
            fileViewer.add(file)
        }
    }

    fun updateFilesViewer(filesViewer: Div, files: MutableList<KFile>, form: FormPanel<FormAddTask>) {
        filesViewer.removeAll()
        if (files.isEmpty()) {
            filesViewer.content = "Файлы не выбраны"
        } else {
            files.forEach { kFile ->
                filesViewer.content = ""
                val fileViewer = Div().apply {
                    add(Div(kFile.name))
                    add(Button("Удалить файл", className = "delete-file-button"){
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
}