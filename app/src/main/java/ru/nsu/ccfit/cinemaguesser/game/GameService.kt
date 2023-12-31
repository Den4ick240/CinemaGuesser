package ru.nsu.ccfit.cinemaguesser.game

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.nsu.ccfit.cinemaguesser.AccountManager
import ru.nsu.ccfit.cinemaguesser.ValueFlow
import ru.nsu.ccfit.cinemaguesser.asValueFlow
import ru.nsu.ccfit.cinemaguesser.mapValue

data class Hint(val hintType: HintType, val values: List<String>)

sealed interface GameEnd {
  data class Success(val score: Int) : GameEnd

  data class Fail(val answer: String) : GameEnd
}

class GameState(
    private val api: GameApi,
    private val accountManager: AccountManager,
    private val coroutineScope: CoroutineScope,
    roundInfo: RoundInfo,
    private val onGameEnd: () -> Unit,
) {
  private val startTime = System.currentTimeMillis()
  private val _roundInfo = MutableStateFlow(roundInfo)
  private val _hintTypes = MutableStateFlow<Result<List<HintType>>>(Result.Success(emptyList()))
  private val hintTypeList =
      _hintTypes.asValueFlow().mapValue { if (it is Result.Success) it.result else emptyList() }

  val hintTypes = _hintTypes.asValueFlow()
  private val _gameEnd = MutableStateFlow<GameEnd?>(null)
  private val _hintList = MutableStateFlow(emptyList<Hint>())
  val hintList: ValueFlow<List<Hint>> = _hintList.asValueFlow()
  val gameEnd = _gameEnd.asValueFlow()
  private val _loading = MutableStateFlow(false)
  val loading = _loading.asValueFlow()

  val score = _roundInfo.asValueFlow().mapValue { it.score }
  val answers = _roundInfo.asValueFlow().mapValue { it.answers }

  val time =
      flow {
            while (_gameEnd.value == null) {
              val ms = System.currentTimeMillis() - startTime
              emit(ms)
              delay(1000)
            }
          }
          .stateIn(coroutineScope, SharingStarted.Lazily, System.currentTimeMillis() - startTime)
          .asValueFlow()

  init {
    coroutineScope.launch { updateHintTypes() }
  }

  private suspend fun updateHintTypes() {
    if (_gameEnd.value != null) return
    _loading.value = true
    _hintTypes.value =
        accountManager.checkAuthorizedResult { api.getAvailableHints(_roundInfo.value.id) }
    _loading.value = false
  }

  suspend fun reloadHintTypes() {
    updateHintTypes()
  }

  suspend fun getHint(hintType: HintType): Result<Hint?> {
    if (hintType !in hintTypeList.value) {
      return Result.Success(null)
    }
    _loading.value = true
    val res =
        accountManager.checkAuthorizedResult {
          val hint = api.getHint(_roundInfo.value.id, hintType) ?: return@checkAuthorizedResult null
          _roundInfo.value = _roundInfo.value.copy(score = hint.score)
          val hintt = Hint(hintType, hint.hint)
          _hintList.value = _hintList.value + listOf(hintt)
          if (!hint.alive) {
            _gameEnd.value = GameEnd.Fail("")
            null
          } else {
            hintt
          }
        }
    reloadHintTypes()
    _loading.value = false
    return res
  }

  suspend fun answer(index: Int): Result<Boolean> {
    val answer = _roundInfo.value.answers[index]
    _loading.value = true
    return accountManager
        .checkAuthorizedResult {
          val (right, alive, newRoundInfo, score) = api.answer(_roundInfo.value.id, answer)
          _roundInfo.value = newRoundInfo ?: _roundInfo.value.copy(score = score)
          if (right) {

            _gameEnd.value = GameEnd.Success(_roundInfo.value.score)
          } else if (!alive) {
            _gameEnd.value = GameEnd.Fail("")
          }

          right
        }
        .also { _loading.value = false }
  }

  suspend fun endGame(): Result<Unit> {
    if (gameEnd.value is GameEnd.Success) {
        _gameEnd.value = null
        updateHintTypes()
        _hintList.value = emptyList()
        return Result.Success(Unit)
    }
    if (_gameEnd.value != null) {
      onGameEnd()
      coroutineScope.cancel()
      return Result.Success(Unit)
    }
    return accountManager
        .checkAuthorized {
          val success = api.endGame(_roundInfo.value.id)
          val res =
              if (success) {
                Result.Success(Unit)
              } else Result.Other()

          res
        }
        .also {
          if (it is Result.Success) {
            onGameEnd()
            coroutineScope.cancel()
          }
        }
  }
}

class GameService(
    private val api: GameApi,
    private val accountManager: AccountManager,
    private val coroutineScope: CoroutineScope,
) {
  private val _game = MutableStateFlow<GameState?>(null)
  val game: ValueFlow<GameState?> = _game.asValueFlow()

  suspend fun startGame(difficulty: Difficulty): Result<Unit> {
    return accountManager.checkAuthorizedResult<Unit> {
      val roundInfo = api.startGame(difficulty)
      _game.value =
          GameState(api, accountManager, CoroutineScope(Dispatchers.Main + Job()), roundInfo) {
            _game.value = null
          }
    }
  }
}

private suspend inline fun <T> AccountManager.checkAuthorized(
    crossinline body: suspend () -> Result<T>
): Result<T> =
    try {
      body()
    } catch (e: NotAuthorizedException) {
      logOut()
      Result.NotAuthorized()
    } catch (e: NoConnectionException) {
      Result.NoInternet()
    }

private suspend inline fun <T> AccountManager.checkAuthorizedResult(
    crossinline body: suspend () -> T
): Result<T> =
    try {
      Result.Success(body())
    } catch (e: NotAuthorizedException) {
      logOut()
      Result.NotAuthorized()
    } catch (e: NoConnectionException) {
      Result.NoInternet()
    }

sealed interface Result<T> {
  class NotAuthorized<T>() : Result<T>

  class NoInternet<T>() : Result<T>

  class Other<T>() : Result<T>

  data class Success<T>(val result: T) : Result<T>
}
