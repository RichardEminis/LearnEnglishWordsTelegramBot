package step2

import org.jetbrains.annotations.Nullable
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
    var correctAnswersCount: Int = 0
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
        val learnedWords = dictionary.filter { word: Word -> word.correctAnswersCount >= 3 }.size
        val totalWords = dictionary.size
        val percent: Double = (learnedWords.toDouble() / totalWords.toDouble()) * 100

        println("Выучено $learnedWords из $totalWords слов | ${percent.toInt()}%")
    }

    fun learnWords() {
        var isExit = true
        val learnedWords = dictionary
        var listOfAnswers: MutableList<String> = mutableListOf()
        val exitToMainMenu = "Для выхода введи 'МЕНЮ'"

        while (isExit) {
            val unlearnedWords = dictionary.filter { word: Word -> word.correctAnswersCount < 3 }

            if (unlearnedWords.isEmpty()) {
                println("Вы выучили все слова")
                return
            }

            for (i in unlearnedWords) {

                if (unlearnedWords.size >= 4) listOfAnswers = unlearnedWords.map { it -> it.translate }.toMutableList()
                else listOfAnswers = learnedWords.map { it -> it.translate }.toMutableList()

                println("${i.text}")

                listOfAnswers.remove(i.translate)
                listOfAnswers.shuffle()
                listOfAnswers = listOfAnswers.take(3).toMutableList()
                listOfAnswers.add(i.translate)
                listOfAnswers.shuffle()

                var iterator = 1
                var convertedWords = ""
                for (i in listOfAnswers) {
                    convertedWords += "${iterator++}) $i\n"
                }

                println("Варианты ответа: \n$convertedWords\n$exitToMainMenu")
                val userAnswer = readln()
                if (userAnswer.equals(i.translate, ignoreCase = true)) {
                    i.correctAnswersCount++
                    println("Верный ответ")
                } else if (userAnswer.equals("меню", ignoreCase = true)) {
                    return
                } else {
                    println("Неверный ответ")
                }
            }
        }
    }
}
