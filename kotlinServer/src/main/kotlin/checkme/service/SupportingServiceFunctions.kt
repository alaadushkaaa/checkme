package checkme.service

import java.sql.ResultSet

internal fun ResultSet.convertToString(): String {
    val data = this.metaData
    val countColumns = data.columnCount
    val rows = mutableListOf<List<String>>()
    while (this.next()) {
        val row = (1..countColumns).map { index ->
            this.getString(index) ?: "NULL"
        }
        rows.add(row)
    }

    val rowsWithSeparator = rows.map { it.joinToString("|") }
    return rowsWithSeparator.joinToString("\n").trim()
}

internal fun ResultSet.getAllTablesNames(): List<String> {
    val data = this.metaData
    val countColumns = data.columnCount
    val tableList = mutableListOf<String>()
    while (this.next()) {
        val tableNames = (1..countColumns).map { index ->
            this.getString(index)
        }.map(String::trim)
        tableList.addAll(tableNames)
    }
    return tableList
}
