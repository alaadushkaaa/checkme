package ru.yarsu.contentPages.content.solutionsPages

import io.kvision.core.onClick
import io.kvision.html.Div
import io.kvision.html.Span
import io.kvision.panel.SimplePanel
import io.kvision.panel.VPanel
import io.kvision.routing.Routing
import io.kvision.tabulator.ColumnDefinition
import io.kvision.tabulator.TableType
import io.kvision.tabulator.TabulatorOptions
import io.kvision.tabulator.tabulator
import ru.yarsu.serializableClasses.solution.SolutionInAdminListsFormat
import ru.yarsu.serializableClasses.solution.UserNameSurname
import ru.yarsu.serializableClasses.task.Criterion
import ru.yarsu.serializableClasses.task.TaskFormatForList
import kotlin.collections.mutableMapOf

class AllSolutionsTableViewer(
    private val routing: Routing,
    private val taskList: List<TaskFormatForList>,
    private val solutionList: List<SolutionInAdminListsFormat>
): SimplePanel() {
    private val listTitle = mutableListOf(
        TaskFormatForList(
            -1,
            "Фамилия Имя",
            mapOf(Pair("", Criterion("", 0, "", ""))),
            mapOf(Pair("", "")),
            "",
            true
        )
    ).apply { this.addAll(taskList) }

    private fun creatingDataMap():  List<Pair<UserNameSurname, MutableMap<String, MutableList<Pair<Int, Pair<String, Int?>>>>>> {
        val userMap = mutableMapOf<UserNameSurname, MutableList<SolutionInAdminListsFormat>>()
        for (solution in solutionList) {
            if (userMap.containsKey(solution.user)) {
                userMap[solution.user]?.add(solution)
            } else {
                if (solution.user != null)
                    userMap.put(solution.user, mutableListOf(solution))
            }
        }
        val newUserMap = mutableMapOf<UserNameSurname, MutableMap<String, MutableList<Pair<Int, Pair<String, Int?>>>>>()
        for ((key, value) in userMap){
            val solutionsMap = mutableMapOf<String, MutableList<Pair<Int, Pair<String, Int?>>>>()
            for (solution in value){
                val taskName = solution.task?.name ?: ""
                if (solutionsMap.containsKey(taskName)) {
                    if ((solution.status == "Проверено")) {
                        val score = solution.result?.values?.map { it.score }?.toList()?.sum() ?: 0
                        solutionsMap[taskName]?.add(Pair(solution.id, Pair(solution.status, score)))
                    } else {
                        solutionsMap[taskName]?.add(Pair(solution.id, Pair(solution.status, null)))
                    }
                } else {
                    if ((solution.status == "Проверено")) {
                        val score = solution.result?.values?.map { it.score }?.toList()?.sum() ?: 0
                        solutionsMap.put(taskName, mutableListOf(Pair(solution.id, Pair(solution.status, score))))
                    } else {
                        solutionsMap.put(taskName, mutableListOf(Pair(solution.id, Pair(solution.status, null))))
                    }
                }
            }
            newUserMap.put(key, solutionsMap)
        }
        return newUserMap.toList().sortedBy { (surname, _) -> surname.surname }
    }

    private val listRows = creatingDataMap()

    private val columns = listTitle.mapIndexed { index, title ->
        ColumnDefinition<Pair<UserNameSurname, MutableMap<String, MutableList<Pair<Int, Pair<String, Int?>>>>>>(
            title.name,
            headerSort = false,
            formatterComponentFunction = { _, _, data ->
                if (index == 0) {
                    Div("${data.first.surname} ${data.first.name}")
                } else {
                    val listDiv = data.second.keys
                    if (listDiv.contains(title.name)) {
                        val divList = data.second[title.name]
                        if (divList != null) {
                            VPanel().apply {
                                this.addAll(
                                    divList.map { value ->
                                        Div("${value.second.first}: ${value.second.second}", className = "cell-content").apply { this.onClick {
                                                routing.navigate("/solution/${value.first}")
                                            }
                                        }
                                    }
                                )
                            }
                        } else {
                            Span("")
                        }
                    } else {
                        Span("")
                    }
                }
            }
        )
    }

    init {
        tabulator(data = listRows, false,
            options = TabulatorOptions(
                columns = columns
            ),
            types = setOf(TableType.BORDERED)
        )
    }
}