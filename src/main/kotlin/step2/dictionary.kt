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
            val unlearnedWords = dictionary.filter { word: Word -> word.correctAnswersCount < 3 }.toMutableList()

            if (unlearnedWords.isEmpty()) {
                println("Вы выучили все слова")
                return
            }

            val numberOfDisplayedWords = 4
            val displayedWords = unlearnedWords.shuffled().take(numberOfDisplayedWords).toMutableList()
            val selectedWord = displayedWords.random()

            if (displayedWords.size < numberOfDisplayedWords) {
                var learnedWords = dictionary.filter { word: Word -> word.correctAnswersCount > 2 }
                learnedWords =
                    learnedWords.shuffled().take(numberOfDisplayedWords - displayedWords.size).toMutableList()
                displayedWords.addAll(learnedWords)
                displayedWords.shuffle()
            }

            println("Изучаемое слово: ${selectedWord.text}")
            println("Варианты ответа: " +
                        "\n1)${displayedWords[0].translate}" +
                        "\n2)${displayedWords[1].translate}" +
                        "\n3)${displayedWords[2].translate}" +
                        "\n4)${displayedWords[3].translate}\n" +
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
