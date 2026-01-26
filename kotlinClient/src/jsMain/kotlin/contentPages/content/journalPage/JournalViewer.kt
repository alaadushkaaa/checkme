package ru.yarsu.contentPages.content.journalPage

import io.kvision.core.onClick
import io.kvision.html.div
import io.kvision.panel.VPanel
import io.kvision.panel.vPanel
import io.kvision.routing.Routing
import ru.yarsu.serializableClasses.logger.LogFileInfo
import kotlin.js.Date

class JournalViewer(
    private val routing: Routing,
    logFiles: List<LogFileInfo>
) : VPanel() {
    init {
        for (logFile in logFiles) {
            vPanel(className = "block cursor-pointer") {
                div("Файл ${logFile.name}", className = "file-name-item")
                div("Дата последнего изменения - ${Date(logFile.lastModified).toLocaleDateString()}")
                div("Размер - ${logFile.size} Б")
            }.onClick {
                routing.navigate("journal/file/${logFile.name}")
            }
        }
    }
}
