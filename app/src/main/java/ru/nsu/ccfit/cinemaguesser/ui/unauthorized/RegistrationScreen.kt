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
import ru.nsu.ccfit.cinemaguesser.R
import ru.nsu.ccfit.cinemaguesser.RegisterResult
import ru.nsu.ccfit.cinemaguesser.Screen
import ru.nsu.ccfit.cinemaguesser.ui.AppTitle
import ru.nsu.ccfit.cinemaguesser.ui.isValidEmail
import ru.nsu.ccfit.cinemaguesser.ui.isValidPassword
import ru.nsu.ccfit.cinemaguesser.ui.isValidUsername

class RegisterViewModel(
    private val accountManager: AccountManager,
) : ViewModel() {
  suspend fun register(username: String, email: String, password: String): RegisterResult {
    return accountManager.register(username, email, password)
  }
}

@Composable
fun RegistrationScreen(navController: NavController, viewModel: RegisterViewModel) {
  Column(
      modifier = Modifier.padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)) {
        AppTitle()
        Text(
            text = stringResource(R.string.registration),
            style = MaterialTheme.typography.titleMedium)

        var generalError by remember { mutableStateOf<String?>(null) }
        generalError?.let { Text(text = it, color = MaterialTheme.colorScheme.error) }

        var email by remember { mutableStateOf(TextFieldValue()) }
        var emailError by remember { mutableStateOf<String?>(null) }
        var username by remember { mutableStateOf(TextFieldValue()) }
        var usernameError by remember { mutableStateOf<String?>(null) }
        val emailFocusRequester = remember { FocusRequester() }
        val passwordFocusRequester = remember { FocusRequester() }
        val secondPasswordFocusRequester = remember { FocusRequester() }
        var passwordError by remember { mutableStateOf<String?>(null) }
        var password by remember { mutableStateOf(TextFieldValue()) }
        var secondPasswordError by remember { mutableStateOf<String?>(null) }
        var secondPassword by remember { mutableStateOf(TextFieldValue()) }
        var loading by remember { mutableStateOf<Boolean>(false) }
        val focusManager = LocalFocusManager.current
        val showPassword = remember { mutableStateOf(false) }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = username,
            onValueChange = {
              usernameError = null
              username = it
            },
            label = { Text(stringResource(R.string.username)) },
            singleLine = true,
            keyboardActions = KeyboardActions(onDone = { emailFocusRequester.requestFocus() }),
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
            modifier = Modifier.focusRequester(emailFocusRequester).fillMaxWidth(),
            value = email,
            onValueChange = {
              emailError = null
              email = it
            },
            label = { Text(stringResource(R.string.email)) },
            singleLine = true,
            keyboardActions = KeyboardActions(onDone = { passwordFocusRequester.requestFocus() }),
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
                    autoCorrect = true,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done),
            keyboardActions =
                KeyboardActions(onDone = { secondPasswordFocusRequester.requestFocus() }),
            visualTransformation =
                if (showPassword.value) VisualTransformation.None
                else PasswordVisualTransformation(),
            trailingIcon = {
              val icon =
                  if (showPassword.value) Icons.Filled.Visibility else Icons.Filled.VisibilityOff

              IconButton(onClick = { showPassword.value = !showPassword.value }) {
                Icon(
                    icon,
                    contentDescription = "Visibility",
                    tint = MaterialTheme.colorScheme.primary)
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
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth().focusRequester(secondPasswordFocusRequester),
            value = secondPassword,
            onValueChange = {
              secondPasswordError = null
              secondPassword = it
            },
            label = { Text(stringResource(R.string.repeat_password)) },
            singleLine = true,
            keyboardOptions =
                KeyboardOptions.Default.copy(
                    autoCorrect = true,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            visualTransformation =
                if (showPassword.value) VisualTransformation.None
                else PasswordVisualTransformation(),
            trailingIcon = {
              val icon =
                  if (showPassword.value) Icons.Filled.Visibility else Icons.Filled.VisibilityOff

              IconButton(onClick = { showPassword.value = !showPassword.value }) {
                Icon(
                    icon,
                    contentDescription = "Visibility",
                    tint = MaterialTheme.colorScheme.primary)
              }
            },
            isError = secondPasswordError != null,
            supportingText = {
              secondPasswordError?.let {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = it,
                    color = MaterialTheme.colorScheme.error)
              }
            },
        )

        val noInternet = stringResource(R.string.connection_error)
        val other = stringResource(R.string.unexpected_error)
        val invalidUsername = stringResource(R.string.invalid_username)
        val invalidPassword = stringResource(R.string.invalid_password)
        val invalidEmail = stringResource(R.string.invalid_email)
        val passwordsDontMatch = stringResource(id = R.string.passwords_dont_match)
        val usernameAlreadyExists = stringResource(id = R.string.username_exists)
        val emailAlreadyExists = stringResource(id = R.string.email_exists)

        fun onRegisterError(registerResult: RegisterResult) {
          when (registerResult) {
            RegisterResult.Success -> {
              navController.navigate(Screen.Unauthorized.Login.route) {
                popUpTo(Screen.Unauthorized.route) { inclusive = true }
              }
            }
            RegisterResult.UsernameExists -> usernameError = usernameAlreadyExists
            RegisterResult.EmailExists -> emailError = emailAlreadyExists
            RegisterResult.NoInternet -> generalError = noInternet
            else -> generalError = other
          }
        }

        val coroutineScope = rememberCoroutineScope()
        Button(
            onClick = {
              if (!isValidUsername(username.text)) usernameError = invalidUsername
              else if (!isValidEmail(email.text)) emailError = invalidEmail
              else if (!isValidPassword(password.text)) passwordError = invalidPassword
              else if (password.text != secondPassword.text)
                  secondPasswordError = passwordsDontMatch
              else
                  coroutineScope.launch {
                    loading = true
                    viewModel
                        .register(username.text, email.text, password.text)
                        .let(::onRegisterError)
                    loading = false
                  }
            }) {
              Text(text = stringResource(R.string.registration_action))
            }
      }
}
