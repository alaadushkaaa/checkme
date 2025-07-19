package ru.yarsu.localStorage

import kotlinx.browser.localStorage

object UserInformationStorage{
    private const val KEY = "INFORMATION"

    fun addUserInformation(userData: String) = localStorage.setItem(KEY, userData)

    fun deleteUserInformation() = localStorage.removeItem(KEY)

    fun isAuthorized() : Boolean = localStorage.getItem(KEY) != null
}