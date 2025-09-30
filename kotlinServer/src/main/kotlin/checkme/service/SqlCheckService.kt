package checkme.service

import checkme.config.CheckDatabaseConfig
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.statement.Statements
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement

const val QUERY_TIMEOUT = 7
// Сервис для работы с временными базами данных, которые создаются для каждой проверки задания с SQL-запросами

class SqlCheckService(
    private val config: CheckDatabaseConfig,
) {
    // todo журнал
    @Suppress("TooGenericExceptionCaught")
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

    private fun createTempDatabase(name: String) {
        val connection = createRootConnection()
        connection.use {
            it.createStatement().execute(
                "CREATE DATABASE `$name`;"
            )
            println("Database $name created")
        }
    }

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
        connection.autoCommit = false
        connection.use {
            val statement = it.createStatement()
            statement.queryTimeout = QUERY_TIMEOUT
            val isResultSet = statement.execute(query)

            return when {
                isResultSet -> {
                    val result = statement.resultSet.convertToString()
                    connection.rollback()
                    result
                }

                else -> getChangesInTables(
                    statement = statement,
                    connection = connection
                )
            }
        }
    }

    private fun getChangesInTables(
        statement: Statement,
        connection: Connection,
    ): String {
        val allTablesList = statement.executeQuery("SHOW TABLES;").getAllTablesNames().sorted()
        val result = mutableListOf<String>()
        for (table in allTablesList) {
            val tableResult = statement.executeQuery("SELECT * FROM `$table`;")
            result.add(tableResult.convertToString())
        }
        connection.rollback()
        changeAutoIncrementValues(
            allTablesList = allTablesList,
            statement = statement
        )
        return result.joinToString("||")
    }

    private fun changeAutoIncrementValues(
        allTablesList: List<String>,
        statement: Statement,
    ) {
        for (table in allTablesList) {
            val autoIncrementColumns = statement.executeQuery(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() " +
                    "AND TABLE_NAME = '$table'" +
                    "AND EXTRA LIKE '%auto_increment%';"
            )
            while (autoIncrementColumns.next()) {
                val autoIncrementColumn = autoIncrementColumns.getString("COLUMN_NAME")
                val maxIdResult = statement.executeQuery(
                    "SELECT COALESCE(MAX(`$autoIncrementColumn`), 0) as max_id FROM `$table`;"
                )
                if (maxIdResult.next()) {
                    val maxId = maxIdResult.getLong(1)
                    val newAutoIncrementValue = maxId + 1
                    statement.executeUpdate(
                        "ALTER TABLE `$table` AUTO_INCREMENT = $newAutoIncrementValue;"
                    )
                    println("В таблице $table AUTO_INCREMENT установлен в $newAutoIncrementValue")
                }
            }
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
