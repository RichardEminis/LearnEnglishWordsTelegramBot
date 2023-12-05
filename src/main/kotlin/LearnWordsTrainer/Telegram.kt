package LearnWordsTrainer

const val STATISTIC_CLICKED = "statistics_clicked"
const val LEARNED_WORDS_CLICKED = "learn_words_clicked"
const val BACK_TO_MENU = "back_to_menu"
const val CALLBACK_DATA_ANSWER_PREFIX = "answer_"
const val RESET_CLICKED = "reset_clicked"
const val TIMING = 2000L
const val TELEGRAM_URL = "https://api.telegram.org/bot"

fun main(args: Array<String>) {

    val telegramBotService = TelegramBotService(
        botToken = args[0]
    )

    var lastUpdateId = 0L
    val trainers = HashMap<Long, LearnWordsTrainer>()

    while (true) {
        Thread.sleep(TIMING)
        val responseString: String = telegramBotService.getUpdates(telegramBotService.botToken, lastUpdateId)

        val response: Response = telegramBotService.json.decodeFromString(responseString)
        if (response.result.isEmpty()) continue
        val sortedUpdates = response.result.sortedBy { it.updateId }
        sortedUpdates.forEach {
            handleUpdate(telegramBotService, it, trainers)
        }
        lastUpdateId = sortedUpdates.last().updateId + 1
    }
}

fun handleUpdate(telegramBotService: TelegramBotService, update: Update, trainers: HashMap<Long, LearnWordsTrainer>) {

    val message = update.message?.text
    val chatId = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val data = update.callbackQuery?.data

    val trainer = trainers.getOrPut(chatId) { LearnWordsTrainer("$chatId.txt") }

    if (message?.lowercase() == "/start") telegramBotService.sendMenu(chatId)

    if (data == LEARNED_WORDS_CLICKED) {
        telegramBotService.checkNextQuestionAndSend(trainer, chatId)
    }

    if (data == STATISTIC_CLICKED) {
        val statistics: Statistics = trainer.getStatistics()
        telegramBotService.sendMessage(
            chatId,
            "Выучено ${statistics.learnedWords} из ${statistics.totalWords} слов | ${statistics.percent}%"
        )
    }

    if (data == BACK_TO_MENU) telegramBotService.sendMenu(chatId)

    if ((data?.startsWith(CALLBACK_DATA_ANSWER_PREFIX) == true)) {
        val indexOfAnswer = data.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt() + 1
        if (trainer.checkAnswer(indexOfAnswer)) {
            telegramBotService.sendMessage(chatId, "Верный ответ!!!")
        } else {
            telegramBotService.sendMessage(
                chatId,
                "\"Не правильно: ${trainer.question?.correctAnswer?.text} - ${trainer.question?.correctAnswer?.translate}\""
            )
        }
        telegramBotService.checkNextQuestionAndSend(trainer, chatId)
    }

    if (data == RESET_CLICKED) {
        trainer.resetProgress()
        telegramBotService.sendMessage(chatId, "Прогресс сброшен")
    }
}