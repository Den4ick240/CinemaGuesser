package ru.nsu.ccfit.cinemaguesser.ui.authorized

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.nsu.ccfit.cinemaguesser.R
import ru.nsu.ccfit.cinemaguesser.Screen
import ru.nsu.ccfit.cinemaguesser.ValueFlow
import ru.nsu.ccfit.cinemaguesser.asValueFlow
import ru.nsu.ccfit.cinemaguesser.collectValueAsState
import ru.nsu.ccfit.cinemaguesser.game.Difficulty
import ru.nsu.ccfit.cinemaguesser.game.GameService
import ru.nsu.ccfit.cinemaguesser.game.Result

class MainScreenViewModel(private val gameService: GameService) : ViewModel() {
  private val _dialogOpen = MutableStateFlow(false)
  val dialogOpen: ValueFlow<Boolean> = _dialogOpen.asValueFlow()
  private val _startRes = MutableStateFlow<Result<Unit>?>(null)
  val startRes = _startRes.asValueFlow()
  private val _loading = MutableStateFlow(false)
  val loading = _loading.asValueFlow()

  val game = gameService.game

  fun openDialog() {
    _dialogOpen.value = true
  }

  fun closeDialog() {
    _dialogOpen.value = false
  }

  fun start(difficulty: Difficulty) {
    viewModelScope.launch {
      _loading.value = true
      _startRes.value = gameService.startGame(difficulty)
      _loading.value = false
    }
  }
}

@Composable
fun MainScreen(navController: NavController, vm: MainScreenViewModel) {
  val game by vm.game.collectValueAsState()

  game?.let { game -> GameScreen(game = game) }
      ?: Column {
        val isDialogOpen by vm.dialogOpen.collectValueAsState()
        if (isDialogOpen) {
          SelectLevel(vm)
        }
        TopBar(navController, vm)
        Content()
      }
}

@Composable
private fun TopBar(navController: NavController, vm: MainScreenViewModel) {
  Row(
      modifier =
          Modifier.fillMaxWidth()
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .padding(4.dp),
      horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(
            painter = painterResource(id = R.drawable.icon_dark),
            contentDescription = null,
        )
        Button(
            onClick = { vm.openDialog() },
            contentPadding = ButtonDefaults.TextButtonContentPadding,
        ) {
          Text(
              text = stringResource(R.string.new_game_btn),
              style = MaterialTheme.typography.bodySmall,
          )
        }
        Spacer(modifier = Modifier.weight(1f))
        ElevatedButton(
            onClick = {},
            contentPadding = ButtonDefaults.TextButtonContentPadding,
        ) {
          Text(
              text = stringResource(R.string.rules_btn),
              style = MaterialTheme.typography.bodySmall,
          )
        }
        ElevatedButton(
            onClick = { navController.navigate(Screen.Authorized.Profile.route) },
            contentPadding = ButtonDefaults.TextButtonContentPadding,
        ) {
          Text(
              text = stringResource(R.string.account_btn),
              style = MaterialTheme.typography.bodySmall,
          )
        }
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectLevel(vm: MainScreenViewModel) {
  val res by vm.startRes.collectValueAsState()
  val loading by vm.loading.collectValueAsState()

  AlertDialog(onDismissRequest = vm::closeDialog) {
    if (loading) {
      Column(
          modifier = Modifier.background(Color.White).padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
      ) {
        Text("Готовим вашу игру, пожалуйста, подождите...")
        CircularProgressIndicator()
      }
    } else {
      Column(
          modifier = Modifier.background(Color.White).padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Text(
            text = stringResource(id = R.string.new_game_btn),
            style = MaterialTheme.typography.headlineMedium)
        when (res) {
          is Result.NoInternet -> Text(stringResource(R.string.connection_error))
          is Result.NotAuthorized -> Text(stringResource(R.string.auth_errror))
          is Result.Other -> Text(stringResource(R.string.unexpected_error))
          else -> Unit
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { vm.start(Difficulty.EASY) },
        ) {
          Text(text = stringResource(id = R.string.easy))
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { vm.start(Difficulty.NORMAL) },
        ) {
          Text(text = stringResource(id = R.string.mid))
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { vm.start(Difficulty.HARD) },
        ) {
          Text(text = stringResource(id = R.string.difficult))
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = vm::closeDialog,
        ) {
          Text(text = stringResource(id = R.string.cancel))
        }
      }
    }
  }
}

@Composable
private fun Content() {
  Card(
      modifier = Modifier.padding(16.dp).fillMaxWidth(),
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
      Text(
          text = stringResource(R.string.greetings),
          style = MaterialTheme.typography.headlineMedium,
      )
      Text(text = stringResource(R.string.instructions))
    }
  }
}
