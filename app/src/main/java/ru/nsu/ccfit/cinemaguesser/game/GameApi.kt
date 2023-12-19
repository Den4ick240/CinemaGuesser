package ru.nsu.ccfit.cinemaguesser.game

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.timeout
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import java.util.concurrent.TimeUnit
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Difficulty {
  EASY,
  NORMAL,
  HARD
}

@Serializable
enum class HintType {
  ACTOR,
  DIRECTOR,
  GENRE,
  KEYWORD,
  IMAGES,
  YEARS,
  RATING,
  COUNTRIES
}

@Serializable
data class RoundInfo(
    val id: Int,
    val score: Int,
    @SerialName("listOfAnswers") val answers: List<String>,
)

@Serializable
data class HintResponse(
    val score: Int = 0,
    @SerialName("parameter") val hint: List<String>,
    @SerialName("alive") val alive: Boolean,
)

@Serializable
data class AnswerResponse(
    val right: Boolean,
    val alive: Boolean,
    val round: RoundInfo?,
    val score: Int,
)

class NotAuthorizedException() : Exception() {}

class NoConnectionException() : Exception() {}

class GameApi(private val client: HttpClient) {
  suspend fun startGame(difficulty: Difficulty): RoundInfo =
      checkNoInternet {
            client.get(GameResource.Start(level = difficulty)) {
              timeout { connectTimeoutMillis  = TimeUnit.MINUTES.toMillis(3) }
            }
          }
          .checkAuthorized()
          .body<RoundInfo>()

  suspend fun getAvailableHints(id: Int): List<HintType> =
      checkNoInternet {
            client.get(GameResource.WithId.GetAvailableHints(GameResource.WithId(id = id)))
          }
          .checkAuthorized()
          .body<List<HintType>>()

  suspend fun getHint(id: Int, type: HintType): HintResponse? =
      checkNoInternet {
            client.get(GameResource.WithId.GetHint(GameResource.WithId(id = id), type))
          }
          .checkAuthorized()
          .also { if (it.bodyAsText() == "Такая подсказка уже была дана.") return null }
          .body<HintResponse>()

  suspend fun answer(id: Int, answer: String): AnswerResponse =
      checkNoInternet {
            client.get(GameResource.WithId.Answer(GameResource.WithId(id = id), answer)) {
              timeout {
                  connectTimeoutMillis = TimeUnit.MINUTES.toMillis(3) }
            }
          }
          .checkAuthorized()
          .body<AnswerResponse>()

  suspend fun endGame(id: Int): Boolean =
      checkNoInternet { client.get(GameResource.WithId.End(GameResource.WithId(id = id))) }
          .checkAuthorized()
          .bodyAsText() == "Игра закончена."

  private fun HttpResponse.checkAuthorized() = also {
    if (it.status == HttpStatusCode.Forbidden) throw NotAuthorizedException()
  }

  private suspend inline fun <T> checkNoInternet(crossinline body: suspend () -> T): T =
      try {
        body()
      } catch (e: Exception) {
        throw NoConnectionException()
      }
}

@Serializable
@Resource("game")
class GameResource {
  @Serializable
  @Resource("start")
  data class Start(
      val parent: GameResource = GameResource(),
      val level: Difficulty,
  )

  @Serializable
  @Resource("")
  data class WithId(
      val parent: GameResource = GameResource(),
      val id: Int,
  ) {
    @Serializable
    @Resource("/getAvailableParameters")
    class GetAvailableHints(
        val parent: WithId,
    )

    @Serializable
    @Resource("/getParameter")
    class GetHint(
        val parent: WithId,
        val type: HintType,
    )

    @Serializable
    @Resource("/setAnswer")
    class Answer(
        val parent: WithId,
        val answer: String,
    )

    @Serializable
    @Resource("/gameEnd")
    class End(
        val parent: WithId,
    )
  }
}
