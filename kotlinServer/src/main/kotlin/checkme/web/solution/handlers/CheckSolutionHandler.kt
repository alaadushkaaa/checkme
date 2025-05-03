package checkme.web.solution.handlers

import checkme.web.auth.forms.SignInRequest
import checkme.web.solution.forms.CheckSolutionRequest
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.http4k.core.*
import org.http4k.lens.MultipartForm
import java.io.File

class CheckSolutionHandler(
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
        val taskId = checkSolutionRequest.task_id
        val form = checkSolutionRequest.form

        val answers = mutableListOf<String>()
        var index = 0

        for (field in form.fields) {
            if (field.key == index.toString()) {
                answers.add(field.value.toString())
                index++
            } else {
                if (form.files.containsKey(index.toString())) {
                    //todo сохранять куда-то файлы загруженные?
                    val file = form.files[index.toString()]
//                    val filePath = "uploads/file-${index}"
//                    File(filePath).writeBytes(file)

                    answers.add(file.toString())
                    index++
                }
            }
        }

        //val checkId : Int = createCheck() //todo база решений
        //solveTask(taskId, checkId) //todo функция проверки

        return Response(Status.OK).body(objectMapper.writeValueAsString(mapOf("checkId", checkId)))
    }
}