package ru.yarsu.contentPages

import io.kvision.html.div
import io.kvision.panel.SimplePanel

class Loading(
    description: String
) : SimplePanel(className="Loading") {
    init {
        div(className="loading-circle")
        div(description)
    }
}