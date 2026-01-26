package ru.yarsu.contentPages.content.journalPage

import io.kvision.html.*
import io.kvision.panel.SimplePanel
import io.kvision.panel.hPanel
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.logger.LogFileInfo

class Journal(
    private val page: Int?,
    private val serverUrl: String,
    private val routing: Routing
) : SimplePanel() {
    init {
        h2("Журнал действий")
        if ((page == null) || (page < 1)) {
            routing.navigate("/journal/1")
        } else {
            hPanel(className = "pagination") {
                button("Назад").onClick {
                    routing.navigate("/journal/${page - 1}")
                }
                div("Страница $page")
                button("Вперёд").onClick {
                    routing.navigate("/journal/${page + 1}")
                }
            }
            loadLogsFilesList(page)
        }
    }

    private fun loadLogsFilesList(
        page: Int?
    ) {
        val requestInit = RequestInit()
        requestInit.method = HttpMethod.GET.name
        requestInit.headers = js("{}")
        requestInit.headers["Authentication"] = "Bearer ${UserInformationStorage.getUserInformation()?.token}"
        window.fetch(serverUrl + "admin/journal/$page", requestInit).then { response ->
            if (response.status.toInt() == 200) {
                response.json().then {
                    val jsonString = JSON.stringify(it)
                    val logFiles = Json.decodeFromString<List<LogFileInfo>>(jsonString)
                    if (logFiles.isEmpty()) {
                        this.add(Div("Нет данных для отображения"))
                    } else {
                        this.add(JournalViewer(routing, logFiles))
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