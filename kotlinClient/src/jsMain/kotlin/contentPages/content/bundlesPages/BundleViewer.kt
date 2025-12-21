package ru.yarsu.contentPages.content.bundlesPages

import io.kvision.core.onClick
import io.kvision.html.div
import io.kvision.html.h2
import io.kvision.html.h3
import io.kvision.html.h4
import io.kvision.panel.VPanel
import io.kvision.panel.hPanel
import io.kvision.routing.Routing
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.serializableClasses.bundle.BundleFormat
import ru.yarsu.serializableClasses.bundle.TaskFormatWithOrder

class BundleViewer(
    private val bundle: BundleFormat,
    private val tasksAndOrders: List<TaskFormatWithOrder>,
    private val routing: Routing
) : VPanel(className = "Bundle") {
    init {
        h2(bundle.name)
        if (UserInformationStorage.isAdmin()) {
            if (bundle.isActual == true) h4("Набор актуален")
            else h4("Набор не является актуальным")

        }
        h3("Задания набора")
        if (tasksAndOrders.isEmpty())  div("Задачи не найдены")
        for (taskAndOrder in tasksAndOrders) {
            hPanel(className = "task-in-list") {
                val taskItem = VPanel(className = "task-item") {
                    div(taskAndOrder.task.name, className = "name")
                    val description = taskAndOrder.task.description
                        .replace("(<([^>]+)>)".toRegex(), "")
                    div(
                        if (description.length > 50)
                            "" + description.filterIndexed { index, _ -> index <= 50 } + "..."
                        else description
                    )
                }.apply {
                    this.onClick {
                        routing.navigate("task/${taskAndOrder.task.id}")
                    }
                }
                this.add(taskItem)
            }
        }
    }

}