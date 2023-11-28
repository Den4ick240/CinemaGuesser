package ru.nsu.ccfit.cinemaguesser.ui.authorized

import android.inputmethodservice.Keyboard
import android.widget.Space
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import ru.nsu.ccfit.cinemaguesser.R
import ru.nsu.ccfit.cinemaguesser.Screen
import ru.nsu.ccfit.cinemaguesser.ui.isValidPassword
import ru.nsu.ccfit.cinemaguesser.ui.isValidUsername

@Composable
fun MainScreen(navController: NavController) {
    Column {
        TopBar(navController)
        Content()
    }
}

@Composable
private fun TopBar(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(painter = painterResource(id = R.drawable.icon_dark), contentDescription = null)
        Button(onClick = {}, contentPadding = ButtonDefaults.TextButtonContentPadding) {
            Text(
                text = stringResource(R.string.new_game_btn),
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        ElevatedButton(onClick = {}, contentPadding = ButtonDefaults.TextButtonContentPadding) {
            Text(
                text = stringResource(R.string.rules_btn),
                style = MaterialTheme.typography.bodySmall
            )
        }
        ElevatedButton(
            onClick = {
                navController.navigate(Screen.Authorized.Profile.route)
            },
            contentPadding = ButtonDefaults.TextButtonContentPadding
        ) {
            Text(
                text = stringResource(R.string.account_btn),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun Content() {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.greetings),
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(text = stringResource(R.string.instructions))
        }
    }
}