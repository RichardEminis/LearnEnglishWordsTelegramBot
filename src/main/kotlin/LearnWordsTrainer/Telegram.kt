package LearnWordsTrainer

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

const val STATISTIC_CLICKED = "statistics_clicked"
const val LEARNED_WORDS_CLICKED = "learn_words_clicked"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"

fun main(args: Array<String>) {

    val botToken = args[0]
    var updateId = 0

            //..............................................................................................

    val trainer = try {
        LearnWordsTrainer()
    } catch (e: Exception) {
        println("Невозможно загрузить словарь")
        return
    }

    trainer.loadDictionary()

    val statistics = trainer.getStatistics()
    val statisticsText = "Выучено ${statistics.learnedWords} из ${statistics.totalWords} слов | ${statistics.percent}%"



    while (true) {
        Thread.sleep(2000)
        val updates: String = getUpdates(botToken, updateId)

        println(updates)

        val question = trainer.getNextQuestion()


        if (getMessage(updates).equals("hello", ignoreCase = true)) sendMessage(botToken, updateId, "Hello!")

        if (getMessage(updates) == "/start") sendMenu(botToken, updateId)

        if (getClickData(updates) == LEARNED_WORDS_CLICKED) sendQuestion(botToken, updateId, question)

        if (getClickData(updates) == STATISTIC_CLICKED) sendMessage(botToken, updateId, statisticsText)



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

    val encoded = URLEncoder.encode(text,StandardCharsets.UTF_8)

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

fun sendMenu(botToken: String, chatId: Int): String{
    val sendMessage = "https://api.telegram.org/bot$botToken/sendMessage"
    val textForRegex = "\"id\":(.+?),"
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

fun sendQuestion(botToken: String, chatId: Int, question: Question?): String{
    val sendMessage = "https://api.telegram.org/bot$botToken/sendMessage"
    val textForRegex = "\"id\":(.+?),"
    val updates = getUpdates(botToken, chatId)
    val chatId = toRegexUpdate(textForRegex, updates)

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
                            "callback_data": "${CALLBACK_DATA_ANSWER_PREFIX + 0} "
                        },
                        {
                            "text": "${variants?.get(1)}",
                            "callback_data": "${CALLBACK_DATA_ANSWER_PREFIX + 1}"
                        },
                        {
                            "text": "${variants?.get(2)}",
                            "callback_data": "${CALLBACK_DATA_ANSWER_PREFIX + 2}"
                        },
                        {
                            "text": "${variants?.get(3)}",
                            "callback_data": "${CALLBACK_DATA_ANSWER_PREFIX + 3}"
                        }
                    ],
                    [
                        {
                            "text": "Меню",
                            "callback_data": "${CALLBACK_DATA_ANSWER_PREFIX + 4} "
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