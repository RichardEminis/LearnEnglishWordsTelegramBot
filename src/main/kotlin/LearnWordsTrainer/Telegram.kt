package LearnWordsTrainer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

const val STATISTIC_CLICKED = "statistics_clicked"
const val LEARNED_WORDS_CLICKED = "learn_words_clicked"
const val BACK_TO_MENU = "back_to_menu"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
const val RESET_CLICKED = "reset_clicked"
const val TIMING = 2000L

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

    val telegramBotService = TelegramBotService(
        botToken = args[0]
    )

    var lastUpdateId = 0L
    val trainers = HashMap<Long, LearnWordsTrainer>()

    while (true) {
        Thread.sleep(TIMING)
        val responseString: String = telegramBotService.getUpdates(telegramBotService.botToken, lastUpdateId)

        val response: Response = telegramBotService.json.decodeFromString(responseString)
        if (response.result.isEmpty()) continue
        val sortedUpdates = response.result.sortedBy { it.updateId }
        sortedUpdates.forEach {
            telegramBotService.handleUpdate(it, trainers)
        }
        lastUpdateId = sortedUpdates.last().updateId + 1
    }
}