package ru.nsu.ccfit.cinemaguesser

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.androidx.compose.koinViewModel
import ru.nsu.ccfit.cinemaguesser.ui.unauthorized.LoginScreen
import ru.nsu.ccfit.cinemaguesser.ui.unauthorized.NewPasswordScreen
import ru.nsu.ccfit.cinemaguesser.ui.unauthorized.PasswordRecoveryScreen
import ru.nsu.ccfit.cinemaguesser.ui.unauthorized.RegistrationScreen
import ru.nsu.ccfit.cinemaguesser.ui.unauthorized.UnauthorizedScreen


class NavigationViewModel(private val accountManager: AccountManager) : ViewModel() {
    val isAuthorized = accountManager.isLoggedIn.asValueFlow()
    fun logOut() {
        accountManager.logOut()
    }
}

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val viewModel = koinViewModel<NavigationViewModel>()
    val isAuthorized by viewModel.isAuthorized.collectValueAsState()

    NavHost(
        navController = navController,
        startDestination = (if (isAuthorized) Screen.Authorized else Screen.Unauthorized).route
    ) {
        composable(Screen.Authorized.route) {
            Button(onClick = { viewModel.logOut() }) {
                Text(text = "Log out")
            }
        }
        composable(Screen.Unauthorized.route) {
            UnauthorizedScreen(navController)
        }
        composable(Screen.Unauthorized.Login.route) {
            LoginScreen(navController, koinViewModel())
        }
        composable(Screen.Unauthorized.Login.PasswordRecovery.route) {
            PasswordRecoveryScreen(navController, koinViewModel())
        }
        composable(Screen.Unauthorized.Register.route) {
            RegistrationScreen(navController, koinViewModel())
        }
        composable(Screen.Unauthorized.Login.PasswordRecovery.NewPassword.route) {
            NewPasswordScreen(navController, koinViewModel())
        }
    }
}