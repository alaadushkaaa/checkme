package checkme.web.solution.handlers

import checkme.domain.checks.Criterion
import checkme.domain.models.AnswerType
import checkme.domain.models.FormatOfAnswer
import checkme.domain.models.Task

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

val task = Task(
    1,
    "Суммирование чисел",
    criterions,
    mapOf("Прикрепите файл" to AnswerType.FILE),
    "Вам необходимо написать " +
        "программу, выполняющую суммирование двух чисел. На вход подаются два числа - a и b, " +
        "в качестве результата - сумма этих чисел. Некорректный ввод необходимо обрабатыввать и " +
        "выводить строку \"Incorrect input\" в случае ошибки"
)
