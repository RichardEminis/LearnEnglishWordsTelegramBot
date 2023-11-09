package step2

import java.io.File

const val NUMBER_OF_DISPLAYED_WORDS = 4
const val MIN_CORRECT_ANSWERS = 2
const val MIN_CORRECT_ANSWERS_FOR_LEARNED = 3


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
        val learnedWords =
            dictionary.filter { word: Word -> word.correctAnswersCount >= MIN_CORRECT_ANSWERS_FOR_LEARNED }.size
        val totalWords = dictionary.size
        val percent: Double = (learnedWords.toDouble() / totalWords.toDouble()) * 100

        println("Выучено $learnedWords из $totalWords слов | ${percent.toInt()}%")
    }

    fun learnWords() {
        while (true) {
            val unlearnedWords =
                dictionary.filter { word: Word -> word.correctAnswersCount < MIN_CORRECT_ANSWERS_FOR_LEARNED }

            if (unlearnedWords.isEmpty()) {
                println("Вы выучили все слова")
                return
            }

            var displayedWords = unlearnedWords.shuffled().take(NUMBER_OF_DISPLAYED_WORDS)
            val selectedWord = displayedWords.random()

            if (displayedWords.size < NUMBER_OF_DISPLAYED_WORDS) {
                val learnedWords = dictionary
                    .filter { word: Word -> word.correctAnswersCount > MIN_CORRECT_ANSWERS }
                    .shuffled()
                    .take(NUMBER_OF_DISPLAYED_WORDS - displayedWords.size)
                displayedWords = (displayedWords + learnedWords).shuffled()
            }

            println("Изучаемое слово: ${selectedWord.text}")
            println("Варианты ответа:")
            for (i in 1..NUMBER_OF_DISPLAYED_WORDS) {
                println("$i)${displayedWords[i - 1].translate}")
            }
            println("Для выхода введи 'МЕНЮ'")

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
