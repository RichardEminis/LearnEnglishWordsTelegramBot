package step1

import java.io.File

fun main() {

    val wordsFile: File = File("words.txt")
    wordsFile.createNewFile()

    for (line in wordsFile.readLines()) println(line)
}
