import bot.Bot

object Main extends App {

  override def main(args: Array[String]) {
    Bot.run()
    Thread.currentThread().join()
  }

}
