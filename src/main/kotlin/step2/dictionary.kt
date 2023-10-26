package step2

import java.io.File

fun main() {
    val wordsFile = File("words.txt")

    val dictionary: MutableList<Word> = mutableListOf()

    for (line in wordsFile.readLines()) {
        val parsedLine = line.split("|")
        dictionary.add(Word(parsedLine[0], parsedLine[1], parsedLine[2]?.toIntOrNull() ?: 0))
    }

    for (i in dictionary) println(i)
}

data class Word(
    val text: String,
    val translate: String,
    val correctAnswersCount: Int?
)