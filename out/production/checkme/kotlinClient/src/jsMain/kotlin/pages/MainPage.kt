package ru.yarsu.pages

import io.kvision.html.button
import io.kvision.html.div
import io.kvision.panel.SimplePanel
import io.kvision.routing.Routing
import ru.yarsu.localStorage.UserInformationStorage

class MainPage(
    private val routing: Routing,
) : SimplePanel() {
    init {
        div("Базовая страница CheckMe")
        button("Выйти") {
            onClick {
                UserInformationStorage.deleteUserInformation()
                routing.navigate("/authorization/sign_in")
            }
        }
    }
}