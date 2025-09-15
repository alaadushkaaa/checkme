package checkme.service

import checkme.config.CheckDatabaseConfig
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.statement.Statements
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

const val QUERY_TIMEOUT = 7
// Сервис для работы с временными базами данных, которые создаются для каждой проверки задания с SQL-запросами

class SqlCheckService(
    private val config: CheckDatabaseConfig,
) {
    //todo журнал
    fun getSqlResults(
        firstScript: String,
        referenceQuery: String,
        studentQuery: String,
        checkId: Int,
        userId: Int,
    ): Result4k<Pair<String, String>, String> {
        val studentUser = "student_${checkId}$userId"
        val studentPass = "student_pass_${checkId}$userId"
        val uniqueDatabaseName = "check${checkId}_user$userId"
        try {
            createTempDatabase(uniqueDatabaseName)
            createDatabaseUser(
                user = studentUser,
                pass = studentPass,
                databaseName = uniqueDatabaseName
            )
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
            return Failure("Error: ${e.message}")
        } finally {
            dropDatabaseAndUserForCheck(
                name = uniqueDatabaseName,
                user = studentUser
            )
            println("Database dropped")
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
            val statement = it.createStatement()
            statement.execute("CREATE USER '$user'@'%' IDENTIFIED BY '$pass'")
            statement.execute("GRANT ALL PRIVILEGES ON `$databaseName`.* TO '$user'@'%'")
            statement.execute("FLUSH PRIVILEGES")
            println("User $user created with access to database $databaseName")
        }
    }

    // для каждого решения создается временная база данных
    private fun createTempDatabase(name: String) {
        val connection = createRootConnection()
        connection.use {
            it.createStatement().execute(
                "CREATE DATABASE `$name`;"
            )
            println("Database $name created")
        }
    }

    // выполнение скрипта с созданием таблиц и их заполнением
    private fun executeFirstScript(
        script: String,
        databaseName: String,
        user: String,
        pass: String,
    ) {
        val statements: Statements = CCJSqlParserUtil.parseStatements(script)
        val connection = createDatabaseConnection(
            name = databaseName,
            user = user,
            pass = pass
        )
        connection.use {
            statements.forEach { statement ->
                it.createStatement().execute(statement.toString())
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
            statement.queryTimeout = QUERY_TIMEOUT
            return statement.executeQuery(query).convertToString()
        }
    }

    private fun createRootConnection(): Connection =
        DriverManager
            .getConnection(config.jdbc, config.user, config.password)

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

        val rowsWithSeparator = rows.map { it.joinToString("|") }
        return rowsWithSeparator.joinToString("\n").trim()
    }

    // удаление базы и пользователя для решения
    private fun dropDatabaseAndUserForCheck(
        name: String,
        user: String,
    ) {
        val connection = createRootConnection()
        connection.use {
            it.createStatement().execute("DROP DATABASE IF EXISTS `$name`;")
            it.createStatement().execute("DROP USER IF EXISTS '$user'@'%';")
        }
    }
}
