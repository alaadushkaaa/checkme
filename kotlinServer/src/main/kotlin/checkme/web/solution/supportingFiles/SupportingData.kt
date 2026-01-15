package checkme.web.solution.supportingFiles

import checkme.domain.checks.Criterion
import checkme.domain.models.AnswerType
import checkme.domain.models.Task
import checkme.web.solution.handlers.COMPLETE_TASK

val criterions = mapOf(
    "Сложение положительных чисел" to
        Criterion(
            "The addition of numbers is correct",
            COMPLETE_TASK,
            "plus_numbers.json",
            "The numbers add up incorrectly"
        ),
    "Invalid input" to
        Criterion(
            "The case of incorrect input is processed",
            COMPLETE_TASK,
            "incorrect_input.json",
            "Do not handle the case of incorrect number input"
        )
)

//val task = Task(
//    1,//так как uuidv7 не знаю на что заменить
//    "Суммирование чисел",
//    criterions,
//    mapOf("Прикрепите файл" to AnswerType.FILE),
//    "Вам необходимо написать " +
//        "программу, выполняющую суммирование двух чисел. На вход подаются два числа - a и b, " +
//        "в качестве результата - сумма этих чисел. Некорректный ввод необходимо обрабатыввать и " +
//        "выводить строку \"Incorrect input\" в случае ошибки",
//    true
//)
