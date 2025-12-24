package ru.yarsu.contentPages.content.hiddenTask

import io.kvision.html.Button
import io.kvision.panel.HPanel
import io.kvision.rest.HttpMethod
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions
import io.kvision.toast.ToastPosition
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import ru.yarsu.localStorage.UserInformationStorage
import ru.yarsu.serializableClasses.ResponseError
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class TaskHiddenButton(
    private val serverUrl: String,
    private var isActual: Boolean,
    @OptIn(ExperimentalUuidApi::class)
    private val taskId: Uuid,
    private val hPanel: HPanel? = null
) : Button("", className = "hidden-button") {
    init {
        text = if (isActual) "Скрыть задачу" else "Показать задачу"
        this.onClick {
            val requestInit = RequestInit()
            requestInit.method = HttpMethod.POST.name
            requestInit.headers = js("{}")
            requestInit.headers["Authentication"] = "Bearer ${UserInformationStorage.getUserInformation()?.token}"
            @OptIn(ExperimentalUuidApi::class)
            window.fetch(serverUrl + "task/change-actuality/$taskId", requestInit).then { response ->
                if (response.status.toInt() == 200) {
                    if (hPanel != null)
                        hPanel.visible = false
                    else {
                        isActual = !isActual
                        text = if (isActual) "Скрыть задачу" else "Показать задачу"
                    }
                } else if (response.status.toInt() == 400) {
                    response.json().then {
                        val jsonString = JSON.stringify(it)
                        val responseUnauthorized =
                            Json.Default.decodeFromString<ResponseError>(jsonString)
                        Toast.danger(responseUnauthorized.error,
                            ToastOptions(
                                duration = 3000,
                                position = ToastPosition.TOPRIGHT,
                            )
                        )
                    }
                } else {
                    Toast.danger("Код ошибки ${response.status}: ${response.statusText}",
                        ToastOptions(
                            duration = 5000,
                            position = ToastPosition.TOPRIGHT,
                        )
                    )
                }
            }
        }
    }
}