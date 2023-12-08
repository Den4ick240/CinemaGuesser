package ru.nsu.ccfit.cinemaguesser.ui.unauthorized

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ru.nsu.ccfit.cinemaguesser.AccountManager
import ru.nsu.ccfit.cinemaguesser.LoginResult
import ru.nsu.ccfit.cinemaguesser.LoginResult.NoInternet
import ru.nsu.ccfit.cinemaguesser.LoginResult.OtherError
import ru.nsu.ccfit.cinemaguesser.LoginResult.PasswordIncorrect
import ru.nsu.ccfit.cinemaguesser.LoginResult.Success
import ru.nsu.ccfit.cinemaguesser.LoginResult.UsernameNotFound
import ru.nsu.ccfit.cinemaguesser.LoginResult.UsernameNotFoundOrPasswordIncorrect
import ru.nsu.ccfit.cinemaguesser.R
import ru.nsu.ccfit.cinemaguesser.Screen
import ru.nsu.ccfit.cinemaguesser.ui.AppTitle
import ru.nsu.ccfit.cinemaguesser.ui.isValidPassword
import ru.nsu.ccfit.cinemaguesser.ui.isValidUsername

class LoginViewModel(private val accountManager: AccountManager) : ViewModel() {
  suspend fun login(username: String, password: String): LoginResult? {
    return accountManager.login(username, password)
  }
}

@Composable
fun LoginScreen(navController: NavController, viewModel: LoginViewModel) {
  Column(
      modifier = Modifier.padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
  ) {
    AppTitle()
    Text(
        text = stringResource(R.string.login),
        style = MaterialTheme.typography.titleMedium,
    )

    val usernameNotFound = stringResource(R.string.username_not_found)
    val passwordIncorrect = stringResource(R.string.password_incorrect)
    val noInternet = stringResource(R.string.connection_error)
    val other = stringResource(R.string.unexpected_error)
    val invalidUsername = stringResource(R.string.invalid_username)
    val invalidPassword = stringResource(R.string.invalid_password)
    var username by remember { mutableStateOf(TextFieldValue()) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var password by remember { mutableStateOf(TextFieldValue()) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf<Boolean>(false) }
    val focusManager = LocalFocusManager.current
    val showPassword = remember { mutableStateOf(false) }
    val passwordFocusRequester = remember { FocusRequester() }
    val usernameOrPasswordIncorrect = stringResource(id = R.string.username_or_password_incorrect)

    fun onLoginError(loginResult: LoginResult) {
      usernameError = null
      passwordError = null
      generalError = null
      when (loginResult) {
        UsernameNotFoundOrPasswordIncorrect -> usernameError = usernameOrPasswordIncorrect
        UsernameNotFound -> usernameError = usernameNotFound
        PasswordIncorrect -> passwordError = passwordIncorrect
        NoInternet -> generalError = noInternet
        OtherError -> generalError = other
        Success ->
            navController.navigate(Screen.Authorized.route) {
              popUpTo(Screen.Unauthorized.route) { inclusive = true }
            }
      }
    }

    generalError?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }

    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = username,
        onValueChange = {
          usernameError = null
          username = it
        },
        label = { Text(stringResource(R.string.username)) },
        singleLine = true,
        keyboardActions = KeyboardActions(onDone = { passwordFocusRequester.requestFocus() }),
        isError = usernameError != null,
        supportingText = {
          usernameError?.let {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = it,
                color = MaterialTheme.colorScheme.error)
          }
        },
    )
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth().focusRequester(passwordFocusRequester),
        value = password,
        onValueChange = {
          passwordError = null
          password = it
        },
        label = { Text(stringResource(R.string.password)) },
        singleLine = true,
        keyboardOptions =
            KeyboardOptions.Default.copy(
                autoCorrect = true, keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        visualTransformation =
            if (showPassword.value) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
          val icon = if (showPassword.value) Icons.Filled.Visibility else Icons.Filled.VisibilityOff

          IconButton(onClick = { showPassword.value = !showPassword.value }) {
            Icon(icon, contentDescription = "Visibility", tint = MaterialTheme.colorScheme.primary)
          }
        },
        isError = passwordError != null,
        supportingText = {
          passwordError?.let {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = it,
                color = MaterialTheme.colorScheme.error)
          }
        },
    )

    val coroutineScope = rememberCoroutineScope()
    Button(
        onClick = {
          if (!isValidUsername(username.text)) usernameError = invalidUsername
          else if (!isValidPassword(password.text)) passwordError = invalidPassword
          else
              coroutineScope.launch {
                loading = true
                viewModel.login(username.text, password.text)?.let(::onLoginError)
                loading = false
              }
        }) {
          Text(text = stringResource(R.string.login_action))
        }

    Button(
        colors = ButtonDefaults.textButtonColors(),
        onClick = { navController.navigate(Screen.Unauthorized.Login.PasswordRecovery.route) }) {
          Text(text = stringResource(R.string.forgot_password))
        }
  }
}
