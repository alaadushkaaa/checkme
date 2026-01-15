package ru.yarsu.contentPages.content.bundlesPages

import io.kvision.html.Div
import io.kvision.html.button
import io.kvision.html.h2
import io.kvision.panel.SimplePanel
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import ru.yarsu.enumClasses.ListType
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.serializableClasses.ResponseError
import ru.yarsu.serializableClasses.bundle.BundleFormat

class BundlesList(
    serverUrl : String,
    private val routing: Routing,
    listType: ListType
) : SimplePanel(){
    init {
        if (listType.ordinal == 0) {
            h2("Список наборов заданий")
        } else {
            h2("Список скрытых наборов")
        }
        if (UserInformationStorage.isAdmin()) {
            button(
                "Cоздать набор",
                className = "usually-button"
            ).onClick { routing.navigate("/add-bundle") }
        }
        val requestInit = RequestInit()
        requestInit.method = HttpMethod.GET.name
        requestInit.headers = js("{}")
        requestInit.headers["Authentication"] = "Bearer ${UserInformationStorage.getUserInformation()?.token}"
        window.fetch(serverUrl + "bundle/${listType.keyWord}", requestInit).then { response ->
            if (response.status.toInt() == 200) {
                response.json().then {
                    val jsonString = JSON.stringify(it)
                    val bundleList = Json.decodeFromString<List<BundleFormat>>(jsonString)
                    if (bundleList.isEmpty()){
                        this.add(Div("Наборы не найдены"))
                    } else {
                        this.add(BundleListViewer(serverUrl, routing, bundleList))
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