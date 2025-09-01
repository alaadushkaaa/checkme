package checkme.service

import checkme.config.CheckDatabaseConfig
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

// Сервис для работы с временными базами данных, которые создаются для каждого решения для проверки
// SQL-запросов
// При каждом новом решении создаются необходимые таблицы для задания с уникальными именами

class SqlCheckService(
    private val config: CheckDatabaseConfig,
) {
    fun getSqlResults(
        firstScript: String,
        referenceQuery: String,
        studentQuery: String,
        checkId: Int,
        userId: Int,
    ): Result4k<Pair<String, String>, String> {
        val studentUser = "student_${checkId}$userId"
        val studentPass = "student_pass_${checkId}$userId}"
        val uniqueDatabaseName = "check${checkId}_user$userId"
        try {
            createDatabaseUser(
                user = studentUser,
                pass = studentPass,
                databaseName = uniqueDatabaseName
            )
            createTempDatabase(uniqueDatabaseName)
            executeFirstScript(
                script = firstScript,
                databaseName = uniqueDatabaseName,
                user = studentUser,
                pass = studentPass
            )
            val studentResult = getQueryResult(
                query = studentQuery,
                databaseName = uniqueDatabaseName,
                user = studentUser,
                pass = studentPass
            )
            val referenceResult = getQueryResult(
                query = referenceQuery,
                databaseName = uniqueDatabaseName,
                user = studentUser,
                pass = studentPass
            )

            return Success(Pair(studentResult, referenceResult))
        } catch (e: Exception) {
            println("Error: ${e.message}")
            return Failure("Error: ${e.message}")
        } finally {
            dropDatabaseAndUserForCheck(
                name = uniqueDatabaseName,
                user = studentUser
            )
        }
    }

    // создание пользователя с любыми правами, но только в одной базе данных
    private fun createDatabaseUser(
        user: String,
        pass: String,
        databaseName: String,
    ) {
        val connection = createRootConnection()
        connection.use {
            it.createStatement().execute(
                "CREATE USER $user@localhost IDENTIFIED BY $pass;\n" +
                        "GRANT ALL PRIVILEGES ON *$databaseName.* TO $user@localhost;"
            )
        }
    }

    // для каждого решения создается временная база данных внутри контейнера
    private fun createTempDatabase(name: String) {
        val connection = createRootConnection()
        connection.use {
            it.createStatement().execute(
                "CREATE DATABASE $name;"
            )
        }
    }

    // выполнение скрипта с созданием таблиц и их заполнением
    private fun executeFirstScript(
        script: String,
        databaseName: String,
        user: String,
        pass: String,
    ) {
        script.split(";")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .forEach { sql ->
                if (sql.isNotBlank()) {
                    val connection = createDatabaseConnection(
                        name = databaseName,
                        user = user,
                        pass = pass
                    )
                    connection.use { it.createStatement().execute(sql) }
                }
            }
    }

    // получаем результат по запросу
    private fun getQueryResult(
        query: String,
        databaseName: String,
        user: String,
        pass: String,
    ): String {
        val connection = createDatabaseConnection(
            name = databaseName,
            user = user,
            pass = pass
        )
        connection.use {
            val statement = it.createStatement()
            return statement.executeQuery(query).convertToString()
        }
    }

    private fun createRootConnection(): Connection =
        DriverManager.getConnection(config.jdbc, config.user, config.password)

    private fun createDatabaseConnection(
        name: String,
        user: String,
        pass: String,
    ): Connection = DriverManager.getConnection("${config.jdbc}/$name", user, pass)

    private fun ResultSet.convertToString(): String {
        val data = this.metaData
        val countColumns = data.columnCount
        val rows = mutableListOf<List<String>>()
        while (this.next()) {
            val row = (1..countColumns).map { index ->
                this.getString(index) ?: "NULL"
            }
            rows.add(row)
        }

        val sortedRows = rows.sortedBy { it.joinToString("|") }
        return sortedRows.joinToString("\n").trim()
    }

    // удаление базы для решения
    private fun dropDatabaseAndUserForCheck(
        name: String,
        user: String,
    ) {
        val connection = createRootConnection()
        connection.use {
            it.createStatement().execute("DROP DATABASE IF EXISTS $name;")
            it.createStatement().execute("DROP USER IF EXISTS $user@localhost;")
        }
    }
}
