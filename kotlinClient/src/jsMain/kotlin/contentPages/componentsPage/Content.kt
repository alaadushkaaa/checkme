package ru.yarsu.contentPages.componentsPage

import io.kvision.panel.SimplePanel
import io.kvision.routing.Routing
import ru.yarsu.contentPages.content.taskListPage.TaskList
import ru.yarsu.enumClasses.ListType

class Content(
    serverUrl : String,
    routing: Routing,
) : SimplePanel() {
    init {
        add(TaskList(serverUrl, routing, ListType.ALL))
    }
}