package LearnWordsTrainer

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {

    val botToken = args[0]
    var updateId = 0

    while (true) {
        Thread.sleep(2000)
        val updates: String = getUpdates(botToken, updateId)

        println(updates)

        if (getMessage(updates).equals("hello", ignoreCase = true)) sendMessage(botToken, updateId, "Hello!")

        sendMenu(botToken, updateId)

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
    val textForRegex = "\"id\":(.+?),"
    val chatId = toRegexUpdate(textForRegex, updates)

    val urlSendMessage = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$text"
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

fun sendMenu(botToken: String, chatId: Int): String{
    val sendMessage = "https://api.telegram.org/bot$botToken/sendMessage"
    val sendMenuBody = """
        {
            "chat_id": $chatId,
            "text": "Основное меню",
            "reply_markup": {
                "inline_keyboard": [
                    [
                        {
                            "text": "Изучить слова",
                            "callback_data": "data1"
                        },
                        {
                            "text": "Статистика",
                            "callback_data": "data2"
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