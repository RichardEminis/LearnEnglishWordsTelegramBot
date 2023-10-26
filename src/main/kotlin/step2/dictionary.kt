package step2

import java.io.File

fun main() {
    val wordsFile = File("words.txt")

    val dictionary: MutableList<Word> = mutableListOf()

    for (line in wordsFile.readLines()) {
        val parsedLine = line.split("|")
        dictionary.add(Word(parsedLine[0], parsedLine[1]))
    }

    for (i in dictionary) println(i)

    val numbersOfWords = File("numbersOfWords.txt")
    val learnedWords = 32
    numbersOfWords.writeText(learnedWords.toString())
}

data class Word(
    val text: String,
    val translate: String,
    val numbersOfWords: File = File("numbersOfWords.txt")
)