package ru.yarsu.contentPages.content.journalPage

import io.kvision.core.onEvent
import io.kvision.form.check.checkBox
import io.kvision.form.number.numericInput
import io.kvision.html.Div
import io.kvision.html.InputType
import io.kvision.html.button
import io.kvision.html.h2
import io.kvision.html.input
import io.kvision.html.span
import io.kvision.panel.SimplePanel
import io.kvision.panel.hPanel
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import kotlinx.browser.window
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.logger.LogFormat

class LogFile(
    private val serverUrl: String,
    private val routing: Routing,
    private val name: String?
) : SimplePanel() {
    init {
        h2("Файл $name") {
            if (name == null) {
                routing.navigate("/journal/1")
            } else {
                loadLogsFromFile(name)
            }
        }
        hPanel {
            button("Назад к списку файлов", className = "navigation-button button-min").onClick {
                routing.navigate("/journal/1")
            }
            button("Обновить", className = "navigation-button button-min").onClick {
                js("window.location.reload()")
            }
        }
    }

    private fun loadLogsFromFile(
        fileName: String,
    ) {
        val requestInit = RequestInit()
        requestInit.method = HttpMethod.GET.name
        requestInit.headers = js("{}")
        requestInit.headers["Authentication"] = "Bearer ${UserInformationStorage.getUserInformation()?.token}"
        window.fetch(serverUrl + "admin/journal/file/$fileName", requestInit).then { response ->
            if (response.status.toInt() == 200) {
                response.json().then {
                    val jsonString = JSON.stringify(it)
                    val logs = Json.decodeFromString<List<LogFormat>>(jsonString)
                    if (logs.isEmpty()) {
                        this.add(Div("Нет данных для отображения"))
                    } else {
                        this.add(LogFileViewer(routing, logs))
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