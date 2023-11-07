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
        while (true) {
            var unlearnedWords = dictionary.filter { word: Word -> word.correctAnswersCount < 3 }.toMutableList()

            if (unlearnedWords.isEmpty()) {
                println("Вы выучили все слова")
                return
            }

            var selectedWord: Word

            if (unlearnedWords.size > 3) {
                unlearnedWords = unlearnedWords.shuffled().toMutableList()
                unlearnedWords = unlearnedWords.take(4).toMutableList()
                selectedWord = unlearnedWords.first()
                unlearnedWords = unlearnedWords.shuffled().toMutableList()
            } else {
                unlearnedWords.addAll(dictionary.filter { word: Word -> word.correctAnswersCount > 2 })
                selectedWord = unlearnedWords.first()
                unlearnedWords = unlearnedWords.shuffled().toMutableList()
                unlearnedWords.remove(selectedWord)
                unlearnedWords.add(selectedWord)
                unlearnedWords = unlearnedWords.takeLast(4).toMutableList()
                unlearnedWords = unlearnedWords.shuffled().toMutableList()
            }

            println("Изучаемое слово: ${selectedWord.text}")
            println("Варианты ответа: " +
                        "\n1)${unlearnedWords[0].translate}" +
                        "\n2)${unlearnedWords[1].translate}" +
                        "\n3)${unlearnedWords[2].translate}" +
                        "\n4)${unlearnedWords[3].translate}\n" +
                        "\nДля выхода введи 'МЕНЮ'")

            val userAnswer = readln()
            if (userAnswer.equals(selectedWord.translate, ignoreCase = true)) {
                selectedWord.correctAnswersCount++
                println("Верный ответ\n")
            } else if (userAnswer.equals("меню", ignoreCase = true)) {
                return
            } else {
                println("Неверный ответ\n")
            }
        }
    }
}
