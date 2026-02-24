package ru.yarsu.contentPages.content.userInfoPage

import io.kvision.html.Div
import io.kvision.panel.SimplePanel
import io.kvision.tabulator.ColumnDefinition
import io.kvision.tabulator.Editor
import io.kvision.tabulator.Formatter
import io.kvision.tabulator.TableType
import io.kvision.tabulator.TabulatorOptions
import io.kvision.tabulator.tabulator
import ru.yarsu.serializableClasses.admin.UserFullData
import io.kvision.rest.HttpMethod
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.serializableClasses.ResponseError
import kotlin.uuid.Uuid
import org.w3c.dom.HTMLElement
import kotlinx.browser.document

class UserInfoTableViewer(
    private val serverUrl: String,
    private val usersData: List<UserFullData>
) : SimplePanel() {
    private val columns = listOf<ColumnDefinition<Map<String, String>>>(
        ColumnDefinition(
            "Логин",
            field = "login",
            headerFilter = Editor.INPUT
        ),
        ColumnDefinition(
            "Фамилия",
            field = "surname",
            headerFilter = Editor.INPUT
        ),
        ColumnDefinition(
            "Имя",
            field = "name",
            headerFilter = Editor.INPUT
        ),
        ColumnDefinition(
            "Пароль",
            field = "password",
            headerFilter = Editor.INPUT
        )
    )

    init {
        tabulator(
            data = getData(),
            options = TabulatorOptions(
                columns = columns +
                    ColumnDefinition(
                        "Действия",
                        field = "action",
                        formatter = Formatter.HTML,
                        cellClick = { event, _ ->
                            val button = event.asDynamic().target
                            if (button && !button.disabled) {
                                val userId = button.getAttribute("user-id")
                                if (userId) changePassword(Uuid.parse(userId))
                            }
                        }
                    )
            ),
            types = setOf(TableType.BORDERED)
        )
    }

    private fun getData(): List<Map<String, String>> {
        return usersData.map { user ->
            mapOf(
                "id" to user.id.toString(),
                "login" to user.login,
                "surname" to user.surname,
                "name" to user.name,
                "password" to if (user.isSystemPass) "Системный" else "Пользовательский",
                "action" to if (user.isSystemPass) "<button class='table-button-inactive' user-id='${user.id}' " +
                        "disabled>Установить системный пароль</button>" else
                    "<button class='table-button' user-id='${user.id}'>Установить системный пароль</button>"
            )
        }
    }

    private fun changePassword(userId: Uuid) {
        val requestInit = RequestInit()
        requestInit.method = HttpMethod.POST.name
        requestInit.headers = js("{}")
        requestInit.headers["Authentication"] = "Bearer ${UserInformationStorage.getUserInformation()?.token}"
        window.fetch(serverUrl + "admin/set-system-password/$userId", requestInit).then { response ->
            if (response.status.toInt() == 200) js("window.location.reload()")
            else if (response.status.toInt() == 400) {
                response.json().then {
                    val jsonString = JSON.stringify(it)
                    val responseError = Json.Default.decodeFromString<ResponseError>(jsonString)
                    this.add(Div(responseError.error, className = "error-message"))
                }
            } else {
                this.add(Div("Код ошибки ${response.status}: ${response.statusText}", className = "error-message"))
            }
        }
    }
}