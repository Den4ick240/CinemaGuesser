package ru.nsu.ccfit.cinemaguesser

import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private const val PrefIsLoggedIn = "pref-is-logged-in"

class AccountManager(
    coroutineScope: CoroutineScope,
    private val sharedPreferences: SharedPreferences,
    private val sharedPreferencesEditor: SharedPreferences.Editor,
) {
    private val _isLoggedIn = MutableStateFlow(sharedPreferences.getBoolean(PrefIsLoggedIn, false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn


    suspend fun login(username: String, password: String): LoginResult? {
        delay(1000)
        return LoginResult.entries.toTypedArray().random()
            .also {
                if (it == LoginResult.Success) {
                    sharedPreferencesEditor.putBoolean(PrefIsLoggedIn, true).apply()
                    _isLoggedIn.value = true
                } else logOut()
            };
    }

    suspend fun register(username: String, email: String, password: String): RegisterResult {
        delay(1000)
        return RegisterResult.entries.toTypedArray().random()
    }

    fun logOut() {
        sharedPreferencesEditor.putBoolean(PrefIsLoggedIn, false).apply()
        _isLoggedIn.value = false
    }

    suspend fun sendCode(email: String): SendCodeResult {
        return SendCodeResult.entries.toTypedArray().random()
    }

    suspend fun verify(text: String): VerifyResult {
        return VerifyResult.entries.toTypedArray().random()
    }

    suspend fun changePassword(password: String): ChangePasswordResult {
        return ChangePasswordResult.entries.toTypedArray().random()
    }
}

enum class LoginResult {
    Success, UsernameNotFound, PasswordIncorrect, NoInternet, OtherError
}

enum class RegisterResult {
    Success, UsernameExists, EmailExists, NoInternet, OtherError
}

enum class SendCodeResult {
    Success, NoInternet, OtherError
}

enum class VerifyResult {
    Success, WrongCode, NoInternet, OtherError
}

enum class ChangePasswordResult {
    Success, NoInternet, OtherError
}