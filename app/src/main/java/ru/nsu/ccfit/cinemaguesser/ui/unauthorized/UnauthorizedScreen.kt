package ru.nsu.ccfit.cinemaguesser.ui.unauthorized

import android.widget.Space
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ru.nsu.ccfit.cinemaguesser.R
import ru.nsu.ccfit.cinemaguesser.Screen
import ru.nsu.ccfit.cinemaguesser.ui.AppTitle


@Composable
fun UnauthorizedScreen(navController: NavHostController) {
    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        AppTitle()
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate(Screen.Unauthorized.Login.route) }) {
            Text(text = stringResource(R.string.login))
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.navigate(Screen.Unauthorized.Register.route) }) {
            Text(text = stringResource(R.string.registration))
        }
    }
}