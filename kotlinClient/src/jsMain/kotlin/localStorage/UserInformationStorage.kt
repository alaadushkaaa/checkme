package ru.yarsu.localStorage

import kotlinx.browser.localStorage
import kotlinx.serialization.json.Json
import ru.yarsu.serializableClasses.user.UserInformation

object UserInformationStorage{
    private const val KEY = "INFORMATION"

    fun addUserInformation(userData: String) = localStorage.setItem(KEY, userData)

    fun deleteUserInformation() = localStorage.removeItem(KEY)

    fun isAuthorized() : Boolean = localStorage.getItem(KEY) != null

    fun getUserInformation() : UserInformation? {
        val jsonString = localStorage.getItem(KEY)
        return if (jsonString != null){
            Json.Default.decodeFromString<UserInformation>(jsonString)
        } else {
            return null
        }
    }

    fun isAdmin() : Boolean = this.getUserInformation()?.role == "ADMIN"
}
