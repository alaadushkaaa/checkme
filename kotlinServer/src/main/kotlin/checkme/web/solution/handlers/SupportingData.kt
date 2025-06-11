package checkme.web.solution.handlers

import checkme.domain.checks.Criterion
import checkme.domain.models.Task

val criterions = mapOf(
    "Сложение положительных чисел" to
        Criterion(
            "Сложение чисел происходит корректно",
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

val task = Task(
    1,
    "Суммирование чисел",
    criterions,
    "Файл",
    "Вам необходимо написать " +
        "программу, выполняющую суммирование двух чисел. На вход подаются два числа - a и b, " +
        "в качестве результата - сумма этих чисел. Некорректный ввод необходимо обрабатыввать и " +
        "выводить строку \"Incorrect input\" в случае ошибки"
)
