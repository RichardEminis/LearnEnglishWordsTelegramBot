package LearnWordsTrainer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

const val STATISTIC_CLICKED = "statistics_clicked"
const val LEARNED_WORDS_CLICKED = "learn_words_clicked"
const val BACK_TO_MENU = "back_to_menu"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
const val RESET_CLICKED = "reset_clicked"

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

fun main(args: Array<String>) {

    val botToken = args[0]
    var lastUpdateId = 0L
    val json = Json { ignoreUnknownKeys = true }
    val trainers = HashMap<Long, LearnWordsTrainer>()

    while (true) {
        Thread.sleep(2000)
        val responseString: String = getUpdates(botToken, lastUpdateId)

        val response: Response = json.decodeFromString(responseString)
        if (response.result.isEmpty()) continue
        val sortedUpdates = response.result.sortedBy { it.updateId }
        sortedUpdates.forEach { handleUpdate(it, json, botToken, trainers) }
        lastUpdateId = sortedUpdates.last().updateId + 1
    }
}

fun handleUpdate(update: Update, json: Json, botToken: String, trainers: HashMap<Long, LearnWordsTrainer>) {

    val message = update.message?.text
    val chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val data = update.callbackQuery?.data

    val trainer =trainers.getOrPut(chatId) {LearnWordsTrainer("$chatId.txt")}

    if (message?.lowercase() == "/start") sendMenu(json, botToken, chatId)

    if (data == LEARNED_WORDS_CLICKED) {
        checkNextQuestionAndSend(json, trainer, botToken, chatId)
    }

    if (data == STATISTIC_CLICKED) {
        val statistics: Statistics = trainer.getStatistics()
        sendMessage(
            json,
            botToken,
            chatId,
            "Выучено ${statistics.learnedWords} из ${statistics.totalWords} слов | ${statistics.percent}%"
        )
    }

    if (data == BACK_TO_MENU) sendMenu(json, botToken, chatId)

    if ((data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true)) {
        val indexOfAnswer = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt() + 1
        if (trainer.checkAnswer(indexOfAnswer)) {
            sendMessage(json, botToken, chatId, "Верный ответ!!!")
        } else {
            sendMessage(
                json, botToken, chatId,
                "\"Не правильно: ${trainer.question?.correctAnswer?.text} - ${trainer.question?.correctAnswer?.translate}\""
            )
        }
        checkNextQuestionAndSend(json, trainer, botToken, chatId)
    }

    if (data == RESET_CLICKED) {
        trainer.resetProgress()
        sendMessage(json, botToken, chatId, "Прогресс сброшен")
    }
}

fun getUpdates(botToken: String, updateId: Long): String {
    val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
    val response = getResponse(urlGetUpdates)

    return response.body()
}

fun sendMessage(json: Json, botToken: String, chatId: Long, message: String): String {
    val sendMessage = "https://api.telegram.org/bot$botToken/sendMessage"
    val requestsBody = SendMessageRequest(
        chatId = chatId,
        text = message,
    )
    val requestBodyString = json.encodeToString(requestsBody)

    val client: HttpClient = HttpClient.newBuilder().build()
    val requests: HttpRequest = HttpRequest.newBuilder().uri(URI.create(sendMessage))
        .header("Content-type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
        .build()

    val response: HttpResponse<String> = client.send(requests, HttpResponse.BodyHandlers.ofString())
    return response.body()
}

fun getResponse(url: String): HttpResponse<String> {
    val client: HttpClient = HttpClient.newBuilder().build()
    val requests: HttpRequest = HttpRequest.newBuilder().uri(URI.create(url)).build()

    return client.send(requests, HttpResponse.BodyHandlers.ofString())
}

fun sendMenu(json: Json, botToken: String, chatId: Long?): String {
    val sendMessage = "https://api.telegram.org/bot$botToken/sendMessage"
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

    val client: HttpClient = HttpClient.newBuilder().build()
    val requests: HttpRequest = HttpRequest.newBuilder().uri(URI.create(sendMessage))
        .header("Content-type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
        .build()

    val response: HttpResponse<String> = client.send(requests, HttpResponse.BodyHandlers.ofString())
    return response.body()
}

fun sendQuestion(json: Json, botToken: String, chatId: Long, question: Question): String {
    val urlGetUpdates = "https://api.telegram.org/bot$botToken/sendMessage"

    val requestsBody = SendMessageRequest(
        chatId = chatId,
        text = question.correctAnswer.text,
        replyMarkup = ReplyMarkup(
            listOf(
                question.variants.mapIndexed { index, word ->
                    InlineKeyboard(
                        text = word.translate, callbackData = "$CALLBACK_DATA_ANSWER_PREFIX$index"
                    )
                },
                listOf(
                    InlineKeyboard(text = "МЕНЮ", callbackData = BACK_TO_MENU)
                )
            )
        )
    )

    val requestBodyString = json.encodeToString(requestsBody)

    val client: HttpClient = HttpClient.newBuilder().build()
    val requests: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates))
        .header("Content-type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
        .build()

    val response: HttpResponse<String> = client.send(requests, HttpResponse.BodyHandlers.ofString())
    return response.body()
}

fun checkNextQuestionAndSend(json: Json, trainer: LearnWordsTrainer, botToken: String, chatId: Long) {
    val question = trainer.getNextQuestion()
    if (question == null) {
        sendMessage(json, botToken, chatId, "Вы выучили все слова")
    } else {
        sendQuestion(json, botToken, chatId, question)
    }
}