package ru.nsu.ccfit.cinemaguesser.ui.unauthorized

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import ru.nsu.ccfit.cinemaguesser.AccountManager
import ru.nsu.ccfit.cinemaguesser.R
import ru.nsu.ccfit.cinemaguesser.Screen
import ru.nsu.ccfit.cinemaguesser.SendCodeResult
import ru.nsu.ccfit.cinemaguesser.VerifyResult
import ru.nsu.ccfit.cinemaguesser.ui.AppTitle
import ru.nsu.ccfit.cinemaguesser.ui.isValidEmail

private const val Seconds = 20_000

class PasswordRecoveryViewModel(private val accountManager: AccountManager) : ViewModel() {
  private val _isCodeSent = MutableStateFlow(false)
  val isCodeSent: Flow<Boolean>
    get() = _isCodeSent

  private var time: Long = 0
  private val _timeLeft = MutableStateFlow<Long>(0)
  val secondLeft = _timeLeft.mapLatest { it.coerceAtLeast(0) / 1000 }

  suspend fun sendCode(email: String): SendCodeResult {
    val res = accountManager.sendCode(email)
    if (res == SendCodeResult.Success) {
      _isCodeSent.value = true
      time = System.currentTimeMillis()
      viewModelScope.launch {
        var timeLeft = Seconds - (System.currentTimeMillis() - time)
        _timeLeft.value = timeLeft
        while (timeLeft > 0) {
          delay(1000)
          timeLeft = Seconds - (System.currentTimeMillis() - time)
          _timeLeft.value = timeLeft
          println("timeLeft: $timeLeft")
        }
      }
    }
    return res
  }

  suspend fun verify(text: String): VerifyResult {
    return accountManager.verify(text).also {
      if (it == VerifyResult.WrongCode) {
        _isCodeSent.value = false
        time = 0L
      }
    }
  }
}

@Composable
fun PasswordRecoveryScreen(navController: NavController, viewModel: PasswordRecoveryViewModel) {
  Column(
      modifier = Modifier.padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)) {
        val isCodeSent by viewModel.isCodeSent.collectAsState(initial = false)
        val secondsLeft by viewModel.secondLeft.collectAsState(initial = 0)

        AppTitle()
        Text(
            text = stringResource(R.string.password_recovery),
            style = MaterialTheme.typography.titleMedium)

        var email by remember { mutableStateOf(TextFieldValue()) }
        var emailError by remember { mutableStateOf<String?>(null) }
        var code by remember { mutableStateOf(TextFieldValue()) }
        var codeError by remember { mutableStateOf<String?>(null) }

        var generalError by remember { mutableStateOf<String?>(null) }
        generalError?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = email,
            onValueChange = {
              emailError = null
              email = it
            },
            label = { Text(stringResource(R.string.email)) },
            singleLine = true,
            isError = emailError != null,
            supportingText = {
              emailError?.let {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = it,
                    color = MaterialTheme.colorScheme.error)
              }
            },
        )

        if (isCodeSent) {
          OutlinedTextField(
              modifier = Modifier.fillMaxWidth(),
              value = code,
              onValueChange = {
                codeError = null
                code = it
              },
              label = { Text(stringResource(R.string.code_label)) },
              singleLine = true,
              isError = codeError != null,
              supportingText = {
                codeError?.let {
                  Text(
                      modifier = Modifier.fillMaxWidth(),
                      text = it,
                      color = MaterialTheme.colorScheme.error)
                }
              },
          )
        }
        val noInternet = stringResource(R.string.connection_error)
        val other = stringResource(R.string.unexpected_error)
        val invalidEmail = stringResource(R.string.invalid_email)
        fun onRes(res: SendCodeResult) {
          return when (res) {
            SendCodeResult.Success -> generalError = null
            SendCodeResult.NoInternet -> generalError = noInternet
            SendCodeResult.OtherError -> generalError = other
          }
        }

        val coroutineScope = rememberCoroutineScope()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)) {
              Button(
                  enabled = secondsLeft == 0L,
                  modifier = Modifier.weight(1f),
                  onClick = {
                    if (!isValidEmail(email.text)) emailError = invalidEmail
                    else coroutineScope.launch { viewModel.sendCode(email.text).let(::onRes) }
                  }) {
                    Text(
                        text =
                            stringResource(
                                if (isCodeSent) R.string.resend_code else R.string.send_code),
                    )
                  }
              if (secondsLeft > 0) {
                Text(
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.seconds_left, secondsLeft),
                )
              }
            }
        val wrongCode = stringResource(id = R.string.wrong_code)
        fun onRes(res: VerifyResult) {
          when (res) {
            VerifyResult.Success ->
                navController.navigate(Screen.Unauthorized.Login.PasswordRecovery.NewPassword.route)
            VerifyResult.WrongCode -> generalError = wrongCode
            VerifyResult.NoInternet -> generalError = noInternet
            VerifyResult.OtherError -> generalError = other
          }
        }
        if (isCodeSent) {
          Button(
              modifier = Modifier.fillMaxWidth(),
              onClick = { coroutineScope.launch { viewModel.verify(code.text).let(::onRes) } }) {
                Text(text = stringResource(R.string.verify))
              }
        }
      }
}
