package checkme.web.solution.handlers

import checkme.db.*
import checkme.domain.accounts.Role
import checkme.domain.models.Check
import checkme.domain.models.User
import checkme.domain.operations.OperationHolder
import checkme.domain.operations.checks.CheckOperationHolder
import checkme.domain.operations.tasks.TaskOperationsHolder
import checkme.web.solution.NEW_SOLUTION
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.forkhandles.result4k.Success
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import org.http4k.core.Body
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.RequestContexts
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.filter.ServerFilters
import org.http4k.lens.MultipartForm
import org.http4k.lens.MultipartFormFile
import org.http4k.lens.RequestContextLens
import org.http4k.lens.Validator
import org.http4k.lens.multipartForm
import org.http4k.routing.*
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CheckSolutionHandlerTest : FunSpec({
    lateinit var operations: OperationHolder
    lateinit var taskOperations: TaskOperationsHolder
    lateinit var checkOperations: CheckOperationHolder
    lateinit var handler: HttpHandler

    val contexts = RequestContexts()
    val objectMapper = jacksonObjectMapper()

    val validCheckResult = Check(validCheckId, validTaskId, validUserId, validDate, validResult, validStatusCorrect)
    val validTaskResult = validTasks.first()
    val validUser = User(
        validUserId,
        validLogin,
        validName,
        validSurname,
        appConfiguredPasswordHasher.hash(validPass),
        Role.STUDENT
    )
    val userLens: RequestContextLens<User?> = mock()

    beforeTest {
        operations = mock()
        checkOperations = mock()
        taskOperations = mock()
        whenever(operations.checkOperations).thenReturn(checkOperations)
        whenever(operations.taskOperations).thenReturn(taskOperations)

        whenever(checkOperations.createCheck).thenReturn(mock())
        whenever(checkOperations.fetchCheckById).thenReturn(mock())
        whenever(checkOperations.fetchAllChecks).thenReturn(mock())
        whenever(checkOperations.updateCheckResult).thenReturn(mock())
        whenever(checkOperations.fetchChecksByUserId).thenReturn(mock())
        whenever(checkOperations.updateCheckStatus).thenReturn(mock())
        whenever(taskOperations.fetchTaskById).thenReturn(mock())

        whenever(userLens(any())).thenReturn(mock())

        val router = routes(
            "$SOLUTION_DIR$NEW_SOLUTION/{taskId}" bind Method.POST to
                CheckSolutionHandler(
                    checkOperations = checkOperations,
                    taskOperations = taskOperations,
                    userLens = userLens
                )
        )
        handler = ServerFilters.InitialiseRequestContext(contexts).then(router)
    }

    test(
        "check solution by valid task id should return OK with check id "
    ) {
        whenever(checkOperations.fetchCheckById(validCheckResult.id)).thenReturn(Success(validCheckResult))
        whenever(checkOperations.createCheck(any(), any(), any(), anyOrNull(), any()))
            .thenReturn(Success(validCheckResult))
        whenever(checkOperations.updateCheckResult(any(), any())).thenReturn(Success(validCheckResult))
        whenever(checkOperations.updateCheckStatus(any(), any())).thenReturn(Success(validCheckResult))

        whenever(taskOperations.fetchTaskById(any())).thenReturn(Success(validTaskResult))

        whenever(userLens(any())).thenReturn(validUser)

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
