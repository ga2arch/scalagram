package traits

import com.pengrad.telegrambot.TelegramBotAdapter

trait BotTrait {
  lazy val bot = {
    println("Init telegramBot")
    TelegramBotAdapter.build(System.getenv("TOKEN"))
  }
}

