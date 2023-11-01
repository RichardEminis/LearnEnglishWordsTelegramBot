package step2

import java.io.File

fun main() {
    val firstDictionary = Dictionary()

    firstDictionary.load()

    while (true) {
        println("Меню: 1 – Учить слова, 2 – Статистика, 0 – Выход")
        when (readln().toInt()) {
            0 -> return
            1 -> println(firstDictionary.learnWords())
            2 -> println(firstDictionary.statistic())
            else -> println("Введено неверное значение")
        }
    }
}

data class Word(
    val text: String,
    val translate: String,
    val correctAnswersCount: Int?
)

class Dictionary(
    val dictionary: MutableList<Word> = mutableListOf()
) {
    fun load() {
        val wordsFile = File("words.txt")
        for (line in wordsFile.readLines()) {
            val parsedLine = line.split("|")
            dictionary.add(Word(parsedLine[0], parsedLine[1], parsedLine[2]?.toIntOrNull() ?: 0))
        }
    }

    fun printDictionary() {
        for (i in dictionary) println(i)
    }

    fun statistic() {
        val learnedWords = dictionary.filter { word: Word -> word.correctAnswersCount!! >= 3 }.size
        val totalWords = dictionary.size
        val percent: Double = (learnedWords.toDouble() / totalWords.toDouble()) * 100

        println("Выучено $learnedWords из $totalWords слов | ${percent.toInt()}%")
    }

    fun learnWords() {
        var isExit = true
        while (isExit) {
            val unlearnedWords = dictionary.filter { word: Word -> word.correctAnswersCount!! < 3 }
            for (i in unlearnedWords) {
                println("${i.text}")
                val listOfAnswers: MutableList<String> = mutableListOf()
                listOfAnswers.add()
                for (i in unlearnedWords){
                    listOfAnswers.add(i.translate)

                }
                println("Варианты ответа: ")
                println("Вернуться в главное меню?")
                isExit = !readln().equals("да", ignoreCase = true)
                if (!isExit) return
            }
        }
    }