package ru.nsu.ccfit.cinemaguesser.ui.authorized

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch
import ru.nsu.ccfit.cinemaguesser.R
import ru.nsu.ccfit.cinemaguesser.collectValueAsState
import ru.nsu.ccfit.cinemaguesser.game.GameEnd
import ru.nsu.ccfit.cinemaguesser.game.GameState
import ru.nsu.ccfit.cinemaguesser.game.Hint
import ru.nsu.ccfit.cinemaguesser.game.HintType
import ru.nsu.ccfit.cinemaguesser.game.HintType.*
import ru.nsu.ccfit.cinemaguesser.game.Result

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(game: GameState) {
  var showDialog by remember { mutableStateOf(false) }
  var selectedHint by remember { mutableStateOf<Hint?>(null) }
  val hintTypes by game.hintTypes.collectValueAsState()
  val hintList by game.hintList.collectValueAsState()
  val score by game.score.collectValueAsState()
  val answers by game.answers.collectValueAsState()
  val enderDragon by game.gameEnd.collectValueAsState()
  val loading by game.loading.collectValueAsState()

  if (loading) {
    AlertDialog(onDismissRequest = {}) {
      Column(
          modifier = Modifier.background(Color.White).padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
      ) {
        Text("Загрузка...")
        CircularProgressIndicator()
      }
    }
  }

  if (hintList.isEmpty()) {
    selectedHint = null
  }
  if (showDialog || selectedHint == null)
      SelectHint(hintTypes, hintList, game = game, dismiss = { showDialog = false }) {
        selectedHint = it
        showDialog = false
      }

  val coroutineScope = rememberCoroutineScope()
  enderDragon?.let {
    AlertDialog(onDismissRequest = { coroutineScope.launch { game.endGame() } }) {
      Column(
          modifier = Modifier.background(Color.White).padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        when (it) {
          GameEnd.Fail -> Text("Вы проиграли", style = MaterialTheme.typography.headlineLarge)
          is GameEnd.Success ->
              Text("Вы выиграли! Очки: ${it.score}", style = MaterialTheme.typography.headlineLarge)
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { coroutineScope.launch { game.endGame() } },
        ) {
          Text(text = "OK")
        }
      }
    }
  }

  Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Card(
        modifier = Modifier.fillMaxWidth().weight(1f),
    ) {
      Column(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        Header(game, score)
        selectedHint?.let { HintView(it) }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { showDialog = true },
        ) {
          Text("Выбрать подсказку")
        }
      }
    }

    val languages = answers
    var selectedAnswer by remember { mutableStateOf(languages[0]) }

    LazyColumn(Modifier.selectableGroup().weight(1f)) {
      languages.forEach { text ->
        item {
          Row(
              Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
          ) {
            RadioButton(
                selected = (text == selectedAnswer),
                onClick = { selectedAnswer = text },
            )
            Text(text = text, fontSize = 22.sp)
          }
        }
      }
    }
    var answerRes by remember { mutableStateOf<Result<Boolean>?>(null) }
    when (val answerRes = answerRes) {
      is Result.NoInternet -> Text(stringResource(R.string.connection_error))
      is Result.NotAuthorized -> Text(stringResource(R.string.auth_errror))
      is Result.Other -> Text(stringResource(R.string.unexpected_error))
      is Result.Success -> if (!answerRes.result) Text(stringResource(R.string.wrong_answer))
      null -> Unit
    }
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
          coroutineScope.launch { answerRes = game.answer(answers.indexOf(selectedAnswer)) }
        },
    ) {
      Text("Отправить ответ")
    }
  }
}

@Composable
private fun Header(game: GameState, score: Int) {
  val time by game.time.collectValueAsState()
  val seconds = TimeUnit.MILLISECONDS.toSeconds(time) % 60
  val minutes = TimeUnit.MILLISECONDS.toMinutes(time) % 60
  val hours = TimeUnit.MILLISECONDS.toHours(time)
  val tt = buildString {
    if (hours > 0) {
      append(hours)
      append(":")
    }
    if (minutes > 0) {
      if (minutes < 10) append(0)
      append(minutes)
      append(":")
      if (seconds < 10) append(0)
    }
    append(seconds)
  }
  Text("Очки: $score, Время: $tt")
}

@Composable
fun ColumnScope.HintView(hint: Hint) {
  Text("Тип подсказки: ${hint.hintType.title()}")
  if (hint.values.isEmpty()) {
    Text(text = "Пусто =(")
  } else if (hint.hintType == IMAGES) {
    BoxWithConstraints(Modifier.weight(1f)) {
      Row(
          Modifier.horizontalScroll(rememberScrollState()),
          verticalAlignment = Alignment.CenterVertically,
      ) {
        hint.values.forEach {
          Image(
              painter = rememberAsyncImagePainter(it),
              contentDescription = null,
              modifier =
                  Modifier.heightIn(max = this@BoxWithConstraints.maxHeight)
                      .widthIn(max = this@BoxWithConstraints.maxWidth),
          )
        }
      }
    }
  } else {
    Column(Modifier.verticalScroll(rememberScrollState()).weight(1f)) {
      Text(
          hint.values.joinToString(", "),
          modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectHint(
    hintTypes: Result<List<HintType>>,
    hintList: List<Hint>,
    game: GameState,
    dismiss: () -> Unit,
    selectHint: (Hint) -> Unit,
) {
  val coroutineScope = rememberCoroutineScope()
  var loading by remember { mutableStateOf(false) }
  AlertDialog(onDismissRequest = dismiss) {
    if (loading) {
      Column(
          modifier = Modifier.background(Color.White).padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
      ) {
        Text("Получаем подсказку...")
        CircularProgressIndicator()
      }
    } else {
      LazyColumn(
          modifier = Modifier.background(Color.White).padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        item {
          Text(
              stringResource(R.string.hint_selection),
              style = MaterialTheme.typography.headlineMedium,
          )
        }
        hintList.forEach {
          item {
            ListItem(
                headlineContent = { Text(it.hintType.title()) },
                trailingContent = { Icon(Icons.Rounded.DownloadDone, contentDescription = null) },
                modifier = Modifier.clickable { selectHint(it) },
            )
          }
        }
        item {
          Text(
              stringResource(R.string.additional_hints),
              style = MaterialTheme.typography.headlineSmall,
          )
        }
        when (val hintTypes = hintTypes) {
          is Result.NoInternet ->
              item {
                Text(
                    stringResource(R.string.connection_error),
                )
              }
          is Result.NotAuthorized ->
              item {
                Text(
                    stringResource(R.string.auth_errror),
                )
              }
          is Result.Other ->
              item {
                Text(
                    stringResource(R.string.unexpected_error),
                )
              }
          is Result.Success -> {
            hintTypes.result.forEach {
              item {
                ListItem(
                    headlineContent = { Text(it.title()) },
                    trailingContent = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    modifier =
                        Modifier.clickable {
                          coroutineScope.launch {
                            loading = true
                            val hint = game.getHint(it)
                            loading = false
                            if (hint is Result.Success && hint.result != null) {
                              selectHint(hint.result)
                            } // NO HOMO
                          } // NO HOMO
                        }, // NO HOMO
                ) // NO HOMO
              } // NO HOMO
            } // NO HOMO
          } // NO HOMO
        } // NO HOMO
      } // NO HOMO
    } // NO HOMO
  } // NO HOMO
} // NO HOMO

@Composable
fun HintType.title() =
    when (this) {
      ACTOR -> stringResource(R.string.actor_hint)
      DIRECTOR -> stringResource(R.string.director_hint)
      GENRE -> stringResource(R.string.genre_hint)
      KEYWORD -> stringResource(R.string.keyword_hint)
      IMAGES -> stringResource(R.string.images_hint)
      YEARS -> stringResource(R.string.years_hint)
      RATING -> stringResource(R.string.rating_hint)
      COUNTRIES -> stringResource(R.string.countries_hint)
    }
