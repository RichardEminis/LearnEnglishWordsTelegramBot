package LearnWordsTrainer

const val NUMBER_OF_DISPLAYED_WORDS = 4
const val MIN_CORRECT_ANSWERS_FOR_LEARNED = 3

fun Question.asConsoleString(): String {
    val variants = this.variants
        .mapIndexed { index, word -> "${index + 1}) ${word.translate}" }
        .joinToString(
            prefix = "Изучаемое слово: ${this.correctAnswer.text}\nВарианты ответа:\n",
            separator = "\n",
            postfix = "\nВведите вариант ответа от 1 до $NUMBER_OF_DISPLAYED_WORDS\nДля выхода введите '0'"
        )
    return variants
}

fun main() {
    val trainer = try {
        LearnWordsTrainer()
    } catch (e: Exception) {
        println("Невозможно загрузить словарь")
        return
    }

    trainer.loadDictionary()

    while (true) {
        println("Меню: 1 – Учить слова, 2 – Статистика, 0 – Выход")
        when (readln().toIntOrNull()) {
            0 -> return
            1 -> {
                while (true) {
                    val question = trainer.getNextQuestion()
                    if (question == null) {
                        println("Вы выучили все слова")
                        break
                    }

                    println(question.asConsoleString())

                    val userAnswerInput = readln().toIntOrNull()
                    if (userAnswerInput == 0) break

                    if (trainer.checkAnswer(userAnswerInput)) {
                        println("Верный ответ!\n")
                    } else {
                        println("Неверный ответ!\n")
                    }
                }
            }

            2 -> {
                val statistics = trainer.getStatistics()
                println("Выучено ${statistics.learnedWords} из ${statistics.totalWords} слов | ${statistics.percent}%")
            }

            else -> println("Введено неверное значение")
        }
    }
}