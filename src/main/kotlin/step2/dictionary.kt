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
    var correctAnswersCount: Int?
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
            if (dictionary.filter { word: Word -> word.correctAnswersCount!! < 3 }.isEmpty()) {
                println("Вы выучили все слова")
                return
            }
            for (i in dictionary.filter { word: Word -> word.correctAnswersCount!! < 3 }) {


                var listOfAnswers: MutableList<String> = mutableListOf()
                for (i in dictionary.filter { word: Word -> word.correctAnswersCount!! < 3 }) {
                    listOfAnswers.add(i.translate)
                }


                println("${i.text}")

                listOfAnswers.remove(i.translate)
                listOfAnswers = listOfAnswers.take(4).toMutableList()
                listOfAnswers.add(i.translate)
                listOfAnswers.shuffle()

                var iterator = 1
                var convertedWords = ""
                for (i in listOfAnswers) {
                    convertedWords += "${iterator++}) $i\n"
                }

                println("Варианты ответа: \n$convertedWords ")
                if (readln().equals(i.translate, ignoreCase = true)) {
                    i.correctAnswersCount = i.correctAnswersCount!! + 1
                    println("Верный ответ")
                } else {
                    println("Неверный ответ")
                }

                if (dictionary.filter { word: Word -> word.correctAnswersCount!! < 3 }.isEmpty()) {
                    println("Вы выучили все слова")
                    return
                }

                println("Вернуться в главное меню?")
                isExit = !readln().equals("да", ignoreCase = true)
                if (!isExit) return
            }
        }
    }
}
