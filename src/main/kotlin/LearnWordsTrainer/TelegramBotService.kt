package LearnWordsTrainer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,
    @SerialName("message")
    val message: Message? = null,
    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null
)

@Serializable
data class Response(
    @SerialName("result")
    val result: List<Update>
)

@Serializable
data class Message(
    @SerialName("text")
    val text: String? = null,
    @SerialName("chat")
    val chat: Chat
)

@Serializable
data class Chat(
    @SerialName("id")
    val id: Long
)

@Serializable
data class SendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long?,
    @SerialName("text")
    val text: String,
    @SerialName("reply_markup")
    val replyMarkup: ReplyMarkup? = null
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyboard: List<List<InlineKeyboard>>
)

@Serializable
data class InlineKeyboard(
    @SerialName("callback_data")
    val callbackData: String,
    @SerialName("text")
    val text: String,
)

@Serializable
data class CallbackQuery(
    @SerialName("data")
    val data: String,
    @SerialName("message")
    val message: Message? = null
)

class TelegramBotService(
    val botToken: String,
    val json: Json = Json { ignoreUnknownKeys = true },
    private val client: HttpClient = HttpClient.newBuilder().build()
) {

    fun getUpdates(botToken: String, updateId: Long): String {
        val urlGetUpdates = "$TELEGRAM_URL$botToken/getUpdates?offset=$updateId"
        val response = getResponse(urlGetUpdates)

        return response.body()
    }

    fun sendMessage(chatId: Long, message: String): String {
        val sendMessage = "$TELEGRAM_URL$botToken/sendMessage"
        val requestsBody = SendMessageRequest(
            chatId = chatId,
            text = message,
        )
        val requestBodyString = json.encodeToString(requestsBody)

        val requests: HttpRequest = HttpRequest.newBuilder().uri(URI.create(sendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()

        val response: HttpResponse<String> = client.send(requests, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    private fun getResponse(url: String): HttpResponse<String> {
        val requests: HttpRequest = HttpRequest.newBuilder().uri(URI.create(url)).build()

        return client.send(requests, HttpResponse.BodyHandlers.ofString())
    }

    fun sendMenu(chatId: Long?): String {
        val sendMessage = "$TELEGRAM_URL$botToken/sendMessage"
        val requestsBody = SendMessageRequest(
            chatId = chatId,
            text = "Основное меню",
            replyMarkup = ReplyMarkup(
                listOf(
                    listOf(
                        InlineKeyboard(text = "Изучать слова", callbackData = LEARNED_WORDS_CLICKED),
                        InlineKeyboard(text = "Статистика", callbackData = STATISTIC_CLICKED)
                    ),
                    listOf(
                        InlineKeyboard(text = "Сброс статистики", callbackData = RESET_CLICKED)
                    )
                )
            )
        )

        val requestBodyString = json.encodeToString(requestsBody)

        val requests: HttpRequest = HttpRequest.newBuilder().uri(URI.create(sendMessage))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()

        val response: HttpResponse<String> = client.send(requests, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    private fun sendQuestion(chatId: Long, question: Question): String {
        val urlGetUpdates = "$TELEGRAM_URL$botToken/sendMessage"

        val listOfQuestions = question.variants.mapIndexed { index, word ->
            listOf(
                InlineKeyboard(
                    text = word.translate, callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index"
                )
            )
        }

        val requestsBody = SendMessageRequest(
            chatId = chatId,
            text = question.correctAnswer.text,
            replyMarkup = ReplyMarkup(
                listOfQuestions + listOf(listOf(InlineKeyboard(text = "МЕНЮ", callbackData = BACK_TO_MENU)))
            )
        )

        val requestBodyString = json.encodeToString(requestsBody)

        val requests: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates))
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
            .build()

        val response: HttpResponse<String> = client.send(requests, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun checkNextQuestionAndSend(trainer: LearnWordsTrainer, chatId: Long) {
        val question = trainer.getNextQuestion()
        if (question == null) {
            sendMessage(chatId, "Вы выучили все слова")
        } else {
            sendQuestion(chatId, question)
        }
    }
}