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
        sendMessage(botToken,updateId)

        val startUpdateId = updates.lastIndexOf("update_id")
        val endUpdateId = updates.lastIndexOf(",\n\"message\"")
        if (startUpdateId == -1 || endUpdateId == -1) continue
        val updateIdString = updates.substring(startUpdateId + 11, endUpdateId)

        updateId = updateIdString.toInt() + 1
    }
}

fun getUpdates(botToken: String, updateId: Int): String {
    val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
    val client: HttpClient = HttpClient.newBuilder().build()
    val requests: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
    val response: HttpResponse<String> = client.send(requests, HttpResponse.BodyHandlers.ofString())

    return response.body()
}

fun sendMessage(botToken: String, updateId: Int){
    val updates = getUpdates(botToken, updateId)
    val messageRegexText: Regex = "\"id\":(.+?),".toRegex()
    val matchResult: MatchResult? = messageRegexText.find(updates)
    val groups = matchResult?.groups
    val text = groups?.get(1)?.value
}