package ru.nsu.ccfit.cinemaguesser.game

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.get
import io.ktor.resources.Resource
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
    val round: RoundInfo,
)

class GameApi(private val client: HttpClient) {
  suspend fun startGame(difficulty: Difficulty): RoundInfo =
      client.get(GameResource.Start(level = difficulty)).body<RoundInfo>()

  suspend fun getAvailableHints(id: Int): List<HintType> =
      client
          .get(GameResource.WithId.GetAvailableHints(GameResource.WithId(id = id)))
          .body<List<HintType>>()

  suspend fun getHint(id: Int, type: HintType): HintResponse =
      client
          .get(GameResource.WithId.GetHint(GameResource.WithId(id = id), type))
          .body<HintResponse>()

  suspend fun answer(id: Int, answer: String): AnswerResponse =
      client
          .get(GameResource.WithId.Answer(GameResource.WithId(id = id), answer))
          .body<AnswerResponse>()
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
        val hintType: HintType,
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
