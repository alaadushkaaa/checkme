package ru.yarsu.contentPages.componentsPage

import io.kvision.panel.SimplePanel
import io.kvision.routing.Routing
import ru.yarsu.contentPages.content.taskListPage.TaskList

class Content(
    serverUrl : String,
    routing: Routing,
) : SimplePanel() {
    init {
        add(TaskList(serverUrl, routing))
    }
}