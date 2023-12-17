package ru.nsu.ccfit.cinemaguesser

import io.ktor.client.HttpClient
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.resources.Resource
import kotlinx.serialization.Serializable

data class PasswordRecoveryResult(
    val message: String,
    val success: Boolean,
)

class PasswordRecoveryApi(
    private val client: HttpClient,
) {

  suspend fun sendCode(email: String) = checkNoInternet {
    val res =
        client.post(PasswordRecovery.SendEmail()) {
          contentType(ContentType.Application.Json)
          setBody(EmailRequest(email))
        }
    PasswordRecoveryResult(
        message = res.bodyAsText(),
        success = res.status == HttpStatusCode.OK,
    )
  }

  suspend fun verifyCode(email: String, code: Int) : PasswordRecoveryResult = checkNoInternet {
    val res =
        client.post(PasswordRecovery.SendEmail()) {
          contentType(ContentType.Application.Json)
          setBody(VerifyCodeRequest(code, email))
        }
    PasswordRecoveryResult(
        message = res.bodyAsText(),
        success = res.status == HttpStatusCode.OK,
    )
  }

  suspend fun setPassword(email: String, code: Int, newPassword: String) = checkNoInternet {
    val res =
        client.post(PasswordRecovery.SendEmail()) {
          contentType(ContentType.Application.Json)
          setBody(NewPasswordRequest(code, email, newPassword))
        }
    PasswordRecoveryResult(
        message = res.bodyAsText(),
        success = res.status == HttpStatusCode.OK,
    )
  }

  private suspend inline fun checkNoInternet(
      crossinline body: suspend () -> PasswordRecoveryResult
  ): PasswordRecoveryResult =
      try {
        body()
      } catch (e: Exception) {
        PasswordRecoveryResult(
            message = "Ошибка соединения, провертье подключение к интернету", success = false)
      }
}

@Serializable
@Resource("auth")
class PasswordRecovery() {
  @Serializable
  @Resource("forgot-password")
  data class SendEmail(val parent: PasswordRecovery = PasswordRecovery())

  @Serializable
  @Resource("check-pass-reset-code")
  data class VerifyCode(val parent: PasswordRecovery = PasswordRecovery())

  @Serializable
  @Resource("set-new-password")
  data class SetPassword(val parent: PasswordRecovery = PasswordRecovery())
}

@Serializable
data class EmailRequest(
    val email: String,
)

@Serializable
data class VerifyCodeRequest(
    val resetCode: Int,
    val email: String,
)

@Serializable
data class NewPasswordRequest(
    val resetCode: Int,
    val email: String,
    val newPassword: String,
)
