package LearnWordsTrainer

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService(
    val botToken: String,
    val json: Json = Json { ignoreUnknownKeys = true }
) {

    fun getUpdates(botToken: String, updateId: Long): String {
        val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
        val response = getResponse(urlGetUpdates)

        return response.body()
    }

    private fun sendMessage(chatId: Long, message: String): String {
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

    private fun getResponse(url: String): HttpResponse<String> {
        val client: HttpClient = HttpClient.newBuilder().build()
        val requests: HttpRequest = HttpRequest.newBuilder().uri(URI.create(url)).build()

        return client.send(requests, HttpResponse.BodyHandlers.ofString())
    }

    private fun sendMenu(chatId: Long?): String {
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

    private fun sendQuestion(chatId: Long, question: Question): String {
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

    fun handleUpdate(update: Update, trainers: HashMap<Long, LearnWordsTrainer>) {

        val message = update.message?.text
        val chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
        val data = update.callbackQuery?.data

        val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt") }

        if (message?.lowercase() == "/start") sendMenu(chatId)

        if (data == LEARNED_WORDS_CLICKED) {
            checkNextQuestionAndSend(trainer, chatId)
        }

        if (data == STATISTIC_CLICKED) {
            val statistics: Statistics = trainer.getStatistics()
            sendMessage(
                chatId,
                "Выучено ${statistics.learnedWords} из ${statistics.totalWords} слов | ${statistics.percent}%"
            )
        }

        if (data == BACK_TO_MENU) sendMenu(chatId)

        if ((data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true)) {
            val indexOfAnswer = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt() + 1
            if (trainer.checkAnswer(indexOfAnswer)) {
                sendMessage(chatId, "Верный ответ!!!")
            } else {
                sendMessage(
                    chatId,
                    "\"Не правильно: ${trainer.question?.correctAnswer?.text} - ${trainer.question?.correctAnswer?.translate}\""
                )
            }
            checkNextQuestionAndSend(trainer, chatId)
        }

        if (data == RESET_CLICKED) {
            trainer.resetProgress()
            sendMessage(chatId, "Прогресс сброшен")
        }
    }

    private fun checkNextQuestionAndSend(trainer: LearnWordsTrainer, chatId: Long) {
        val question = trainer.getNextQuestion()
        if (question == null) {
            sendMessage(chatId, "Вы выучили все слова")
        } else {
            sendQuestion(chatId, question)
        }
    }
}