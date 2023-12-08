package ru.nsu.ccfit.cinemaguesser

import android.app.Application
import android.content.SharedPreferences
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import ru.nsu.ccfit.cinemaguesser.game.GameApi
import ru.nsu.ccfit.cinemaguesser.ui.authorized.ProfileViewModel
import ru.nsu.ccfit.cinemaguesser.ui.unauthorized.LoginViewModel
import ru.nsu.ccfit.cinemaguesser.ui.unauthorized.NewPasswordViewModel
import ru.nsu.ccfit.cinemaguesser.ui.unauthorized.PasswordRecoveryViewModel
import ru.nsu.ccfit.cinemaguesser.ui.unauthorized.RegisterViewModel

val appModule = module {
  single { getSharedPrefs(androidApplication()) }

  single<SharedPreferences.Editor> { getSharedPrefs(androidApplication()).edit() }
  factory { CoroutineScope(Dispatchers.IO + SupervisorJob()) }
  factoryOf(::TokenStore)
  factoryOf(::GameApi)
  singleOf(::AccountManager)
  viewModelOf(::NavigationViewModel)
  viewModelOf(::LoginViewModel)
  viewModelOf(::RegisterViewModel)
  viewModelOf(::PasswordRecoveryViewModel)
  viewModelOf(::NewPasswordViewModel)
  viewModelOf(::ProfileViewModel)

  factory {
    val tokenStore = get<TokenStore>()
    HttpClient {
      defaultRequest {
        //                url("https://tomcat.csfullstack.com/cinema-guesser-api/api/v1/")
        url("http://10.0.2.2:8080/api/v1/")
      }
      install(Resources) {}
      install(ContentNegotiation) { json(Json) }
      install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.ALL
      }
      install(Auth) {
        bearer {
          loadTokens { tokenStore.bearerTokens }
          refreshTokens {
            // TODO save refreshed token
            val token =
                client
                    .get {
                      markAsRefreshTokenRequest()
                      url("auth/refreshToken")
                      header("Authorization", "Bearer ${tokenStore.refreshToken}")
                    }
                    .body<RefreshResponse>()
            tokenStore.bearerTokens
          }
        }
      }
    }
  }
}

fun getSharedPrefs(androidApplication: Application): SharedPreferences {
  return androidApplication.getSharedPreferences("default", android.content.Context.MODE_PRIVATE)
}

private val TokenStore.bearerTokens
  get() = BearerTokens(accessToken = accessToken, refreshToken = refreshToken)

@Serializable
data class RefreshResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
)
