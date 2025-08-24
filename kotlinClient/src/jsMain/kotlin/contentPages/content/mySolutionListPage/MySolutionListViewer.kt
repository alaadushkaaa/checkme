package ru.yarsu.contentPages.content.mySolutionListPage

import io.kvision.core.onClick
import io.kvision.html.div
import io.kvision.panel.VPanel
import io.kvision.panel.vPanel
import io.kvision.routing.Routing
import ru.yarsu.serializableClasses.solution.SolutionInListFormat
import kotlinx.datetime.LocalDateTime


class MySolutionListViewer(
    solutionList: List<SolutionInListFormat>,
    private val routing: Routing
) : VPanel() {
    init {
        for (solution in solutionList){
            vPanel(className = "block") {
                div(solution.task.name)
                val dateTime = LocalDateTime.parse(solution.date)
                div("${solution.status} - ${dateTime.date} ${dateTime.time}")
            }.onClick {
                routing.navigate("solution/${solution.id}")
            }
        }
    }
}