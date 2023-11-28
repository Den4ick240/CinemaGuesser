package ru.nsu.ccfit.cinemaguesser

import android.content.SharedPreferences
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.resources.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private const val PrefIsLoggedIn = "pref-is-logged-in"
private const val PrefAccessToken = "pref-access-token"
private const val PrefRefreshToken = "pref-refresh-token"

class TokenStore(
    private val sharedPreferences: SharedPreferences,
    private val sharedPreferencesEditor: SharedPreferences.Editor,
) {
    var accessToken: String
        get() = sharedPreferences.getString(PrefAccessToken, "") ?: ""
        set(value) {
            sharedPreferencesEditor.putString(PrefAccessToken, value)
        }
    var refreshToken: String
        get() = sharedPreferences.getString(PrefRefreshToken, "") ?: ""
        set(value) {
            sharedPreferencesEditor.putString(PrefRefreshToken, value)
        }
}

class AccountManager(
    coroutineScope: CoroutineScope,
    private val sharedPreferences: SharedPreferences,
    private val sharedPreferencesEditor: SharedPreferences.Editor,
    private val tokenStore: TokenStore,
    private val client: HttpClient,
) {
    private val _isLoggedIn = MutableStateFlow(sharedPreferences.getBoolean(PrefIsLoggedIn, false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    suspend fun login(username: String, password: String): LoginResult {
        val request = AuthenticateRequest(username, password)

        val re = try {
            client.post(AuthApi.Authenticate()) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return LoginResult.NoInternet
        }
        when (re.bodyAsText()) {
            "Ошибка авторизации. Проверьте правильонсть ввода данных." -> return LoginResult.UsernameNotFoundOrPasswordIncorrect
        }

        try {
            val authResponse = re.body<AuthenticateResponse>()
            tokenStore.accessToken = authResponse.accessToken
            tokenStore.refreshToken = authResponse.refreshToken
        } catch (e: Exception) {
            e.printStackTrace()
            return LoginResult.OtherError
        }

        sharedPreferencesEditor.putBoolean(PrefIsLoggedIn, true).apply()
        _isLoggedIn.value = true
        return LoginResult.Success
    }

    suspend fun register(username: String, email: String, password: String): RegisterResult {
        val request = RegisterRequest(username, email, password)
        val re = try {
            client.post(AuthApi.Register()) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return RegisterResult.NoInternet
        }
        when (re.bodyAsText()) {
            "Пользователь с таким логином уже есть." -> return RegisterResult.UsernameExists
            "Пользователь с такой почтой уже есть." -> return RegisterResult.EmailExists
        }

        try {
            re.body<RegisterResponse>()
        } catch (e: Exception) {
            e.printStackTrace()
            return RegisterResult.OtherError
        }

        return RegisterResult.Success
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
    Success, UsernameNotFoundOrPasswordIncorrect, UsernameNotFound, PasswordIncorrect, NoInternet, OtherError
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

@Serializable
data class AuthenticateResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
)

@Serializable
data class AuthenticateRequest(
    val username: String,
    val password: String,
)

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val role: String = "USER"
)

@Serializable
data class RegisterResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
)


@Serializable
@Resource("auth")
class AuthApi() {
    @Serializable
    @Resource("register")
    class Register(val parent: AuthApi = AuthApi())

    @Resource("authenticate")
    @Serializable
    class Authenticate(val parent: AuthApi = AuthApi())
}
