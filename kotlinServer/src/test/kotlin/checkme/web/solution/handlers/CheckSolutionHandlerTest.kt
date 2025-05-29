package checkme.web.solution.handlers

import checkme.db.*
import checkme.domain.models.Check
import checkme.domain.models.CheckType
import checkme.domain.models.Task
import checkme.domain.operations.OperationHolder
import checkme.domain.operations.checks.CheckOperationHolder
import checkme.web.solution.NEW_SOLUTION
import checkme.web.solution.checks.CheckDataConsole
import checkme.web.solution.checks.Criterion
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.forkhandles.result4k.Success
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import org.http4k.core.*
import org.http4k.filter.ServerFilters
import org.http4k.lens.MultipartForm
import org.http4k.lens.MultipartFormFile
import org.http4k.lens.Validator
import org.http4k.lens.multipartForm
import org.http4k.routing.*
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CheckSolutionHandlerTest : FunSpec({
    lateinit var operations: OperationHolder
    lateinit var checkOperations: CheckOperationHolder
    lateinit var handler: HttpHandler

    val contexts = RequestContexts()
    val objectMapper = jacksonObjectMapper()
    val validCriterions = mapOf(
        "Сложение положительных чисел" to
            Criterion(
                "Сложение чисел происходит корреткно",
                COMPLETE_TASK,
                "plus_numbers.json",
                "Числа складываются неправильно"
            ),
        "Некорректный ввод" to
            Criterion(
                "Случай некоректного ввода обрабатывается",
                COMPLETE_TASK,
                "incorrect_input.json",
                "Не обработан случай некорректного ввода чисел"
            )
    )
    val validTask = Task(
        1,
        "Суммирование чисел",
        validCriterions,
        "Файл",
        "Вам необходимо написать " +
            "программу, выполняющую суммирование двух чисел. На вход подаются два числа - a и b, " +
            "в качестве результата - сумма этих чисел. Некорректный ввод необходимо обрабатыввать и " +
            "выводить строку \"Incorrect input\" в случае ошибки"
    )
    val validChecks = listOf(
        CheckDataConsole(
            CheckType.CONSOLE_CHECK,
            "printf \"5\\n3\\n\" | python3 sum.py",
            "8.0",
        ),
        CheckDataConsole(
            CheckType.CONSOLE_CHECK,
            "printf \\\"a\\\\n\\\" | python3 sum.py",
            "Incorrect input",
        )
    )

    val validCheckResult = Check(validCheckId, validTaskId, validUserId, validDate, validResult, validStatusCorrect)

    beforeTest {
        operations = mock()
        checkOperations = mock()
        whenever(operations.checkOperations).thenReturn(checkOperations)

        whenever(checkOperations.createCheck).thenReturn(mock())
        whenever(checkOperations.fetchCheckById).thenReturn(mock())
        whenever(checkOperations.fetchAllChecks).thenReturn(mock())
        whenever(checkOperations.updateCheckResult).thenReturn(mock())
        whenever(checkOperations.fetchChecksByUserId).thenReturn(mock())
        whenever(checkOperations.updateCheckStatus).thenReturn(mock())

        val router = routes(
            "$SOLUTION_DIR$NEW_SOLUTION/{taskId}" bind Method.POST to CheckSolutionHandler(checkOperations)
        )
        handler = ServerFilters.InitialiseRequestContext(contexts).then(router)
    }

    test(
        "check solution by valid task id should return OK with check id "
    ) {
        whenever(checkOperations.fetchCheckById(validCheckResult.id)).thenReturn(Success(validCheckResult))
        whenever(checkOperations.createCheck(any(), any(), any(), anyOrNull(), any())).thenReturn(Success(validCheckResult))
        whenever(checkOperations.updateCheckResult(any(), any())).thenReturn(Success(validCheckResult))
        whenever(checkOperations.updateCheckStatus(any(), any())).thenReturn(Success(validCheckResult))

        val filePart = MultipartFormFile(
            filename = "solution.txt",
            contentType = ContentType.TEXT_PLAIN,
            content = "test solution".byteInputStream()
        )

        val form = MultipartForm(
            files = mapOf("ans" to listOf(filePart)),
            fields = emptyMap(),
            errors = emptyList()
        )
        val lens = Body.multipartForm(Validator.Feedback, MultipartFormFile.multi.required("ans")).toLens()
        val request = Request(Method.POST, "$SOLUTION_DIR$NEW_SOLUTION/1")
            .header("Content-Type", "multipart/form-data")
            .let { lens(form, it) }
        val response = handler(request)
        response.status.shouldBe(Status.OK)
        val responseBody = objectMapper.readValue<Map<String, String>>(response.body.toString())
        responseBody["checkId"].shouldMatch(Regex("([0-9]+)"))
    }
})
