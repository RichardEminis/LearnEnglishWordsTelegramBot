package LearnWordsTrainer

import java.io.File
import java.lang.IllegalStateException

data class Word(
    val text: String,
    val translate: String,
    var correctAnswersCount: Int = 0
)

data class Statistics(
    val learnedWords: Int,
    val totalWords: Int,
    val percent: Double
)

data class Question(
    var variants: List<Word>,
    val correctAnswer: Word
)

class LearnWordsTrainer(
    val dictionary: MutableList<Word> = mutableListOf(),
    var question: Question? = null,
    val trainer: String = ""
) {

    fun loadDictionary() {
        try {
            val wordsFile = File("words.txt")
            for (line in wordsFile.readLines()) {
                val parsedLine = line.split("|")
                dictionary.add(Word(parsedLine[0], parsedLine[1], parsedLine[2]?.toIntOrNull() ?: 0))
            }
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException("Некорректный файл")
        }
    }

    fun saveDictionary(dictionary: List<Word>) {
        val wordsFile = File("words.txt")
        var savedText = ""
        for (i in dictionary) {
            val line = "${i.text}|${i.translate}|${i.correctAnswersCount}\n"
            savedText += line
        }
        wordsFile.writeText(savedText)
    }

    fun getStatistics(): Statistics {
        val learnedWords =
            dictionary.filter { word: Word -> word.correctAnswersCount >= MIN_CORRECT_ANSWERS_FOR_LEARNED }.size
        val totalWords = dictionary.size
        val percent: Double = (learnedWords.toDouble() / totalWords.toDouble()) * 100

        return Statistics(learnedWords, totalWords, percent)
    }

    fun getNextQuestion(): Question? {
        val unlearnedWords = dictionary.filter { it.correctAnswersCount < MIN_CORRECT_ANSWERS_FOR_LEARNED }
        if (unlearnedWords.isEmpty()) return null
        var displayedWords = unlearnedWords.shuffled().take(NUMBER_OF_DISPLAYED_WORDS)
        val selectedWord = displayedWords.random()

        if (unlearnedWords.size < NUMBER_OF_DISPLAYED_WORDS) {
            val learnedWords = dictionary
                .filter { word: Word -> word.correctAnswersCount >= MIN_CORRECT_ANSWERS_FOR_LEARNED }
                .shuffled()
                .take(NUMBER_OF_DISPLAYED_WORDS - unlearnedWords.size)
            displayedWords = (unlearnedWords + learnedWords).shuffled()
        }
        question = Question(variants = displayedWords, correctAnswer = selectedWord)
        return question
    }

    fun checkAnswer(userAnswerIndex: Int?): Boolean {
        return question?.let {
            if (it.variants.indexOf(it.correctAnswer) + 1 == userAnswerIndex) {
                it.correctAnswer.correctAnswersCount++
                saveDictionary(dictionary)
                true
            } else {
                false
            }
        } ?: false
    }
}