package ru.nsu.ccfit.cinemaguesser

import androidx.navigation.NavController

sealed class Screen(val route: String) {
    data object Authorized : Screen("authorized"){
        data object Game : Screen("game")
        data object Rules : Screen("rules")
        data object Profile : Screen("profile")
    }

    data object Unauthorized : Screen("unauthorized") {
        data object Register : Screen("registration")
        data object Login : Screen("login") {
            data object PasswordRecovery : Screen("forgotpass") {
                data object NewPassword : Screen("newpassword")
            }
        }
    }
}

fun NavController.navigate(screen : Screen) = navigate(screen.route)