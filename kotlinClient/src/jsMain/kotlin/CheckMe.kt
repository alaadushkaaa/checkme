package ru.yarsu

import io.kvision.Application
import io.kvision.Hot
import io.kvision.html.button
import io.kvision.html.div
import io.kvision.panel.root
import io.kvision.routing.Routing
import io.kvision.startApplication
import io.kvision.utils.useModule
import ru.yarsu.authorization.SignIn
import ru.yarsu.authorization.SignUp
import ru.yarsu.localStorage.UserInformationStorage

@JsModule("./css/App.css")
external val cssApp: dynamic

@JsModule("./css/Authorization.css")
external val cssAuthorization: dynamic

@JsModule("./css/ErrorMessage.css")
external val cssErrorMessage: dynamic

class CheckMe : Application() {
    init {
        useModule(cssApp)
        useModule(cssAuthorization)
        useModule(cssErrorMessage)
    }
    override fun start() {
        val serverUrl = "http://localhost:9999/"
        val routing = Routing.init("/")
        val applicationRoot = root("checkMe") { }
        routing.on("/authorization/sign_in", {
            applicationRoot.removeAll()
            if (UserInformationStorage.isAuthorized()){
                routing.navigate("/")
            } else {
                val signIn = SignIn(serverUrl, routing)
                applicationRoot.add(signIn)
            }
        }).on("/authorization/sign_up", {
            applicationRoot.removeAll()
            if (UserInformationStorage.isAuthorized()){
                routing.navigate("/")
            } else {
                val signUp = SignUp(serverUrl, routing)
                applicationRoot.add(signUp)
            }
        }).on("/", {
            applicationRoot.removeAll()
            if (UserInformationStorage.isAuthorized()){
                applicationRoot.div("Базовая страница CheckMe")
                applicationRoot.button("Выйти") {
                    onClick {
                        UserInformationStorage.deleteUserInformation()
                        routing.navigate("/authorization/sign_in")
                    }
                }
            }
            else {
                routing.navigate("/authorization/sign_in")
            }
        }).resolve()
    }
}

fun main() {
    startApplication(
        ::CheckMe,
        js("import.meta.webpackHot").unsafeCast<Hot?>()
    )
}