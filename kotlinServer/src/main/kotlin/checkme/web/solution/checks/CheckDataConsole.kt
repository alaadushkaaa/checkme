package checkme.web.solution.checks

import checkme.domain.models.CheckType
import checkme.domain.models.Task
import checkme.web.solution.forms.CheckResult
import java.io.File
import java.util.concurrent.TimeUnit

// Извлекается команда из CheckDataConsole, после чего она запускается в нужной директории с решением пользователя
// Результат команды сравнивается с ожидаемым expected CheckDataConsole, после чего присваиваются баллы.
// В случае некорректного теста студенту по данному критерию присваивается 0 баллов, а в сообщение записывается ошибка

data class CheckDataConsole(
    val type: CheckType,
    val command: String,
    val expected: String,
) {
    companion object {
        fun consoleCheck(
            task: Task,
            checkDataConsole: CheckDataConsole,
            checkId: Int,
            criterion: Criterion,
        ): CheckResult {
            val command = checkDataConsole.command
            val output = runCommandInDirectory("абсолютный/путь/resources/solutions/solution$checkId", command)
            if (output.startsWith("Error:")) {
                return CheckResult(
                    0,
                    "При выполнении теста {${criterion.test} задания " +
                        "${task.name}-${task.id} произошла ошибка: $output"
                )
            }
            return if (output.trim() != checkDataConsole.expected.trim()) {
                CheckResult(0, criterion.message)
            } else {
                CheckResult(criterion.score, criterion.description)
            }
        }

        private fun runCommandInDirectory(
            directory: String,
            command: String,
        ): String {
            val process = ProcessBuilder("bash", "-c", command)
                .directory(File(directory))
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()

            if (!process.waitFor(MINUTE_TIMEOUT.toLong(), TimeUnit.SECONDS)) {
                process.destroy()
                return "Error: Вышло время на процесс"
            }
            println(output)
            return if (error.isBlank()) output else "Error: $error"
        }
    }
}

const val MINUTE_TIMEOUT = 60
