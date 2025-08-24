package ru.yarsu.contentPages.componentsPage

import io.kvision.html.TAG
import io.kvision.html.p
import io.kvision.html.tag
import io.kvision.panel.VPanel

class Footer : VPanel(className = "Footer"){
    init {
        tag(TAG.HR)
        p("Сервис находится в разработке.")
    }
}