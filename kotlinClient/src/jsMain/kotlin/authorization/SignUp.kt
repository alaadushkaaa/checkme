package ru.yarsu.authorization

import io.kvision.form.formPanel
import io.kvision.form.text.Password
import io.kvision.form.text.Text
import io.kvision.html.button
import io.kvision.html.h2
import io.kvision.panel.HPanel
import io.kvision.panel.VPanel
import io.kvision.rest.HttpMethod
import io.kvision.routing.Routing
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
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
) : VPanel(className = "Authorization") {
    init {
        h2("Регистрация в системе")
        val formPanelSignUp = formPanel<FormSignUp>(className = "authorization-card") {
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
        formPanelSignUp.add(HPanel(className = "authorization-buttons-panel") {
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
                                Toast.danger(responseUnauthorized.error,
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
            }
            button("Вход").onClick{
                routing.navigate("/authorization/sign_in")
            }
        })
    }
}