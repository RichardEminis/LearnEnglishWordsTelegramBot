package step1

import java.io.File

fun main() {

    val wordsFile = File("words.txt")

    for (line in wordsFile.readLines()) println(line)
}
