package ru.yarsu

import io.kvision.Application
import io.kvision.Hot
import io.kvision.panel.root
import io.kvision.routing.Routing
import io.kvision.startApplication
import io.kvision.utils.useModule
import ru.yarsu.authorization.SignIn
import ru.yarsu.authorization.SignUp
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.contentPages.MainPage

@JsModule("./css/App.css")
external val cssApp: dynamic

@JsModule("./css/Authorization.css")
external val cssAuthorization: dynamic

@JsModule("./css/ErrorMessage.css")
external val cssErrorMessage: dynamic

@JsModule("./css/Header.css")
external val cssHeader: dynamic

@JsModule("./css/Footer.css")
external val cssFooter: dynamic

@JsModule("./css/TaskForm.css")
external val cssTaskForm: dynamic

@JsModule("./css/TasksList.css")
external val cssTasksList: dynamic

@JsModule("./css/Task.css")
external val cssTask: dynamic

@JsModule("./css/UserList.css")
external val cssUserList: dynamic

@JsModule("./css/Result.css")
external val cssResult: dynamic

@JsModule("./css/Loading.css")
external val cssLoading: dynamic

class CheckMe : Application() {
    init {
        useModule(cssAuthorization)
        useModule(cssErrorMessage)
        useModule(cssHeader)
        useModule(cssFooter)
        useModule(cssApp)
        useModule(cssTaskForm)
        useModule(cssTasksList)
        useModule(cssTask)
        useModule(cssUserList)
        useModule(cssResult)
        useModule(cssLoading)
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
        }).on("*", {
            applicationRoot.removeAll()
            if (UserInformationStorage.isAuthorized()){
                val mainPage = MainPage(serverUrl, routing)
                applicationRoot.add(mainPage)
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