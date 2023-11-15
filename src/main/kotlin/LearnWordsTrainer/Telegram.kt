package LearnWordsTrainer

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {

    val botToken = args[0]
    val urlGetMe = "https://api.telegram.org/bot$botToken/getMe"

    val client: HttpClient = HttpClient.newBuilder().build()

    val requests: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetMe)).build()

    val response: HttpResponse<String> = client.send(requests, HttpResponse.BodyHandlers.ofString())

    println(response.body())
}