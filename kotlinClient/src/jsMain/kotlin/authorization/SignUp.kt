package ru.yarsu.authorization

import io.kvision.form.formPanel
import io.kvision.form.text.Password
import io.kvision.form.text.Text
import io.kvision.html.Div
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.panel.HPanel
import io.kvision.panel.VPanel
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.serializableClasses.FormSignUp
import ru.yarsu.serializableClasses.RequestSignUp
import ru.yarsu.serializableClasses.ResponseUnauthorized

class SignUp(
    private val serverUrl: String,
    private val routing: Routing,
) : VPanel(spacing = 5) {
    init {
        this.visible = false
        div("Регистрация в системе")
        val formPanelSignUp = formPanel<FormSignUp> {
            add(
                FormSignUp::login,
                Text(label = "Логин") { placeholder = "Введите логин" },
                required = true,
                requiredMessage = "Вы не ввели логин"
            )
            add(
                FormSignUp::name,
                Text(label = "Имя") { placeholder = "Введите ваше имя" },
                required = true,
                requiredMessage = "Вы не ввели имя"
            )
            add(
                FormSignUp::surname,
                Text(label = "Фамилия") { placeholder = "Введите вашу фамилию" },
                required = true,
                requiredMessage = "Вы не ввели фамилию"
            )
            add(
                FormSignUp::password,
                Password(label = "Пароль") { placeholder = "Введите пароль" },
                required = true,
                requiredMessage = "Вы не ввели Пароль"
            )
            add(
                FormSignUp::passwordRepeat,
                Password(label = "Повторный пароль") { placeholder = "Повторите пароль" },
                required = true,
                requiredMessage = "Вы повторно не ввели пароль"
            )
            validator = {
                if (it[FormSignUp::password] != it[FormSignUp::passwordRepeat]) {
                    it.getControl(FormSignUp::passwordRepeat)?.validatorError = "Пароль не совпадает"
                }
                it[FormSignUp::password] == it[FormSignUp::passwordRepeat]
            }
        }
        formPanelSignUp.add(HPanel {
            button("Зарегистрироваться").onClick {
                val validateForm = formPanelSignUp.validate()
                if (validateForm) {
                    val requestInit = RequestInit()
                    requestInit.method = HttpMethod.POST.name
                    requestInit.headers = js("{}")
                    requestInit.headers["Content-Type"] = "application/json"
                    requestInit.body = Json.Default.encodeToString(
                        RequestSignUp(
                            formPanelSignUp.getData().login,
                            formPanelSignUp.getData().name,
                            formPanelSignUp.getData().surname,
                            formPanelSignUp.getData().password
                        )
                    )
                    window.fetch(serverUrl + "user/sign_up", requestInit).then { response ->
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
                                formPanelSignUp.add(
                                    Div(responseUnauthorized.error)
                                        .apply {
                                            window.setTimeout({ visible = false }, 3000)
                                        }
                                )
                            }
                        } else {
                            formPanelSignUp.add(
                                Div("Код ошибки ${response.status}: ${response.statusText}")
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