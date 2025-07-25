package ru.yarsu.authorization

import io.kvision.form.formPanel
import io.kvision.form.text.Password
import io.kvision.form.text.Text
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.panel.HPanel
import io.kvision.panel.VPanel
import ru.yarsu.serializableClasses.FormSignIn
import io.kvision.html.Div
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.serializableClasses.RequestSignIn
import ru.yarsu.serializableClasses.ResponseUnauthorized

class SignIn(
    private val serverUrl: String,
    private val routing: Routing,
) : VPanel(spacing = 5, className = "authorization-card") {
    init {

        div("Вход в систему")
        val formPanelSignIn = formPanel<FormSignIn> {
            add(
                FormSignIn::login,
                Text(label="Логин") { placeholder = "Введите логин" },
                required = true,
                requiredMessage = "Вы не ввели логин"
            )
            add(
                FormSignIn::password,
                Password(label="Пароль") { placeholder = "Введите пароль"},
                required = true,
                requiredMessage = "Вы не ввели пароль"
            )
        }
        formPanelSignIn.add(HPanel {
            button("Войти", className = "authorization-buttons-panel").onClick {
                val validateForm = formPanelSignIn.validate()
                if (validateForm) {
                    val requestInit = RequestInit()
                    requestInit.method = HttpMethod.POST.name
                    requestInit.headers = js("{}")
                    requestInit.headers["Content-Type"] = "application/json"
                    requestInit.body = Json.Default.encodeToString(
                        RequestSignIn(
                            formPanelSignIn.getData().login,
                            formPanelSignIn.getData().password
                        )
                    )
                    window.fetch(serverUrl + "user/sign_in", requestInit).then { response ->
                        if (response.status.toInt() == 201) {
                            response.json().then {
                                val jsonString = JSON.stringify(it)
                                UserInformationStorage.addUserInformation(jsonString)
                                routing.navigate("/")
                            }
                        } else if (response.status.toInt() == 401) {
                            response.json().then {
                                val jsonString = JSON.stringify(it)
                                val responseUnauthorized =
                                    Json.Default.decodeFromString<ResponseUnauthorized>(jsonString)
                                formPanelSignIn.add(
                                    Div(responseUnauthorized.error, className = "error-message")
                                        .apply {
                                            window.setTimeout({ visible = false }, 3000)
                                        }
                                )
                            }
                        } else {
                            formPanelSignIn.add(
                                Div("Код ошибки ${response.status}: ${response.statusText}", className = "error-message")
                                    .apply {
                                        window.setTimeout({ visible = false }, 3000)
                                    }
                            )
                        }
                    }
                }
            }
        })
    }
}