package ru.yarsu.enumClasses

enum class ResultType(val code: String, val keyWord: String){
    LIST("list", "all"),
    TASK("task", "task"),
    HIDDEN("hiddenList", "hidden"),
}