package LearnWordsTrainer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

const val STATISTIC_CLICKED = "statistics_clicked"
const val LEARNED_WORDS_CLICKED = "learn_words_clicked"
const val BACK_TO_MENU = "back_to_menu"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"

fun main(args: Array<String>) {

    @Serializable
    data class Update(
        @SerialName("update_id")
        val updateId: Long
    )
    @Serializable
    data class Response(
        @SerialName("result")
        val result: List <Update>
    )

    val json = Json{
        ignoreUnknownKeys = true
    }

    val responseString = """
        {
            "ok": true,
            "result": [
                {
                    "update_id": 611141957,
                    "message": {
                        "message_id": 391,
                        "from": {
                            "id": 1130198888,
                            "is_bot": false,
                            "first_name": "Aleksandr",
                            "last_name": "Belyaev",
                            "username": "RichardEminis",
                            "language_code": "ru"
                        },
                        "chat": {
                            "id": 1130198888,
                            "first_name": "Aleksandr",
                            "last_name": "Belyaev",
                            "username": "RichardEminis",
                            "type": "private"
                        },
                        "date": 1701379663,
                        "text": "/start",
                        "entities": [
                            {
                                "offset": 0,
                                "length": 6,
                                "type": "bot_command"
                            }
                        ]
                    }
                }
            ]
        }
    """.trimIndent()

    val response= json.decodeFromString<Response>(responseString)




    //...................................................

    val botToken = args[0]
    var updateId = 0

    val trainer = try {
        LearnWordsTrainer()
    } catch (e: Exception) {
        println(sendMessage(botToken, updateId, "Невозможно загрузить словарь"))
        return
    }

    trainer.loadDictionary()

    val statistics = trainer.getStatistics()
    val statisticsText = "Выучено ${statistics.learnedWords} из ${statistics.totalWords} слов | ${statistics.percent}%"

    while (true) {
        Thread.sleep(2000)
        val updates: String = getUpdates(botToken, updateId)

        println(updates)

        if (getMessage(updates).equals("hello", ignoreCase = true)) sendMessage(botToken, updateId, "Hello!")

        if (getMessage(updates) == "/start") sendMenu(botToken, updateId)

        if (getClickData(updates) == LEARNED_WORDS_CLICKED) checkNextQuestionAndSend(trainer, botToken, updateId)

        if (getClickData(updates) == STATISTIC_CLICKED) sendMessage(botToken, updateId, statisticsText)

        if (getClickData(updates) == BACK_TO_MENU) sendMenu(botToken, updateId)

        if ((getClickData(updates)?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true)) {
            val indexOfAnswer = getClickData(updates)?.substringAfter(CALLBACK_DATA_ANSWER_PREFIX)?.toInt()
            if (trainer.checkAnswer(indexOfAnswer)) {
                sendMessage(botToken, updateId, "Верный ответ!!!")
            } else {
                sendMessage(botToken, updateId,
                    "\"Не правильно: ${trainer.question?.correctAnswer?.text} - ${trainer.question?.correctAnswer?.translate}\""
                )
            }
            checkNextQuestionAndSend(trainer, botToken, updateId)
        }

        updateId = (getUpdateId(updates)?.toInt() ?: 0) + 1
    }
}

fun getUpdates(botToken: String, updateId: Int): String {
    val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
    val response = getResponse(urlGetUpdates)

    return response.body()
}

fun sendMessage(botToken: String, updateId: Int, text: String): String {
    val updates = getUpdates(botToken, updateId)
    val textForRegex = "\"chat\":\\{\"id\":(\\d+),"
    val chatId = toRegexUpdate(textForRegex, updates)
    println(chatId)

    val encoded = URLEncoder.encode(text, StandardCharsets.UTF_8)

    val urlSendMessage = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$encoded"
    val response = getResponse(urlSendMessage)

    return response.body()
}

fun getMessage(updates: String): String? {
    val textForRegex = "\"text\":\"(.+?)\""
    val text = toRegexUpdate(textForRegex, updates)

    return text
}

fun toRegexUpdate(textToRegex: String, updates: String): String? {
    val messageRegexText: Regex = textToRegex.toRegex()
    val matchResult: MatchResult? = messageRegexText.find(updates)
    val groups = matchResult?.groups
    val text = groups?.get(1)?.value

    return text
}

fun getResponse(url: String): HttpResponse<String> {
    val client: HttpClient = HttpClient.newBuilder().build()
    val requests: HttpRequest = HttpRequest.newBuilder().uri(URI.create(url)).build()
    val response: HttpResponse<String> = client.send(requests, HttpResponse.BodyHandlers.ofString())

    return response
}

fun getUpdateId(updates: String): String? {
    val textForRegex = "\"update_id\":(.+?),"
    val updateId = toRegexUpdate(textForRegex, updates)

    return updateId
}

fun getClickData(updates: String): String? {
    val textForRegex = "\"data\":\"(.+?)\""
    val dataRegex = toRegexUpdate(textForRegex, updates)

    return dataRegex
}

fun sendMenu(botToken: String, chatId: Int): String {
    val sendMessage = "https://api.telegram.org/bot$botToken/sendMessage"
    val textForRegex = "\"chat\":\\{\"id\":(\\d+),"
    val updates = getUpdates(botToken, chatId)
    val chatId = toRegexUpdate(textForRegex, updates)
    val sendMenuBody = """
        {
            "chat_id": $chatId,
            "text": "Основное меню",
            "reply_markup": {
                "inline_keyboard": [
                    [
                        {
                            "text": "Изучить слова",
                            "callback_data": "$LEARNED_WORDS_CLICKED"
                        },
                        {
                            "text": "Статистика",
                            "callback_data": "$STATISTIC_CLICKED"
                        }
                    ]
                ]
            }
        }
    """.trimIndent()

    val client: HttpClient = HttpClient.newBuilder().build()
    val requests: HttpRequest = HttpRequest.newBuilder().uri(URI.create(sendMessage))
        .header("Content-type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
        .build()

    val response: HttpResponse<String> = client.send(requests, HttpResponse.BodyHandlers.ofString())

    return response.body()
}

fun sendQuestion(botToken: String, chatId: Int, question: Question?): String {
    val sendMessage = "https://api.telegram.org/bot$botToken/sendMessage"
    val textForRegex = "\"chat\":\\{\"id\":(\\d+),"
    val updates = getUpdates(botToken, chatId)
    val chatId = toRegexUpdate(textForRegex, updates)
    println(chatId)

    val variants = question?.variants?.mapIndexed { index, word -> word.translate }

    val sendMenuBody = """
        {
            "chat_id": $chatId,
            "text": "${question?.correctAnswer?.text}",
            "reply_markup": {
                "inline_keyboard": [
                    [
                        {
                            "text": "${variants?.get(0)}",
                            "callback_data": "${CALLBACK_DATA_ANSWER_PREFIX + 1}"
                        },
                        {
                            "text": "${variants?.get(1)}",
                            "callback_data": "${CALLBACK_DATA_ANSWER_PREFIX + 2}"
                        },
                        {
                            "text": "${variants?.get(2)}",
                            "callback_data": "${CALLBACK_DATA_ANSWER_PREFIX + 3}"
                        },
                        {
                            "text": "${variants?.get(3)}",
                            "callback_data": "${CALLBACK_DATA_ANSWER_PREFIX + 4}"
                        }
                    ],
                    [
                        {
                            "text": "Меню",
                            "callback_data": "$BACK_TO_MENU"
                        }
                    ]
                ]
            }
        }
    """.trimIndent()

    val client: HttpClient = HttpClient.newBuilder().build()
    val requests: HttpRequest = HttpRequest.newBuilder().uri(URI.create(sendMessage))
        .header("Content-type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
        .build()

    val response: HttpResponse<String> = client.send(requests, HttpResponse.BodyHandlers.ofString())

    return response.body()
}

fun checkNextQuestionAndSend(trainer: LearnWordsTrainer, botToken: String, chatId: Int) {
    val question = trainer.getNextQuestion()
    if (question == null) {
        sendMessage(botToken, chatId, "Вы выучили все слова")
    } else {
        sendQuestion(botToken, chatId, question)
    }
}