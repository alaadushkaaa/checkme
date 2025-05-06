package checkme.web.solution.handlers

import checkme.domain.models.Check
import checkme.domain.operations.checks.CheckOperationHolder
import checkme.domain.operations.checks.CreateCheckError
import checkme.web.solution.forms.CheckSolutionRequest
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import org.http4k.core.*
import java.time.LocalDateTime

class CheckSolutionHandler(
    private val checkOperations: CheckOperationHolder,
) : HttpHandler {
    override fun invoke(request: Request): Response {
//        1. получение id проверки и id задания для проверки
//        2. получение пути к файлам задания (откуда будем брать проверки) + создание папки
//                для проверки и переход в нее
//        3. получение задания из бд
//        4. получение критериев проверки задания (от задания)
//        6. перебор критериев из файла (если есть название теста - берем соотвествующий из папки с проверками)
//        7. пытаемся по типу проверки записать json проверки в data class по соответствию какой-либо проверке
//        8. выполнение соответствующих тестов (по каждому заполняем  result[criterion] = {}
//                result[criterion]['score'] +  result[criterion]['message'))
//        9. Сохранение результатов проверки в бд (таблица проверок)
//        database.checks.update_one(
//            {'_id': ObjectId(check_id)},
//            {'$set': { 'result': result, 'status': 'Проверено' }}
//        )
//        beforeAll.py - выполняется перед всеми тестами.
//        beforeEach.py - выполняется перед каждым тестом.
//        afterEach.py - выполняется после каждого теста.
//        afterAll.py - выполняется после всех тестов.
//        Если возникает ошибка, статус меняется на "Ошибка выполнения"

//        request.auth_user: авторизированный пользователь
//        task_id: "*строковый идентификатор задания*"
//        form: поля формы

        val objectMapper = jacksonObjectMapper()
        val checkSolutionRequest = objectMapper.readValue<CheckSolutionRequest>(request.bodyString())
        val taskId = checkSolutionRequest.taskId
        val form = checkSolutionRequest.form

        val answers = mutableListOf<String>()
        var index = 0

        for (field in form.fields) {
            if (field.key == index.toString()) {
                answers.add(field.value.toString())
                index++
            } else {
                if (form.files.containsKey(index.toString())) {
                    // todo сохранять куда-то файлы загруженные?
                    val file = form.files[index.toString()]
//                    val filePath = "uploads/file-${index}"
//                    File(filePath).writeBytes(file)

                    answers.add(file.toString())
                    index++
                }
            }
        }

        return when (val newCheck = createNewCheck(taskId.toInt(), checkSolutionRequest.authUser.id)) {
            is Failure -> Response(Status.INTERNAL_SERVER_ERROR)
                .body(objectMapper.writeValueAsString(mapOf("error" to newCheck.reason.errorText)))
            is Success -> {
                // solveTask(taskId, checkId) //todo функция проверки
                return Response(Status.OK).body(objectMapper.writeValueAsString(mapOf("checkId" to newCheck.value.id)))
            }
        }
    }

    private fun createNewCheck(
        taskId: Int,
        userId: Int,
    ): Result<Check, CreationCheckError> {
        return when (
            val newCheck = checkOperations.createCheck(
                taskId,
                userId,
                LocalDateTime.now(),
                null,
                "В процессе"
            )
        ) {
            is Failure -> when (newCheck.reason) {
                CreateCheckError.UNKNOWN_DATABASE_ERROR -> Failure(CreationCheckError.UNKNOWN_DATABASE_ERROR)
            }

            is Success -> Success(newCheck.value)
        }
    }
}

enum class CreationCheckError(val errorText: String) {
    UNKNOWN_DATABASE_ERROR("Что-то случилось. Пожалуйста, повторите попытку позднее или обратитесь за помощью"),
}
