import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.pengrad.telegrambot.{TelegramBot, TelegramBotAdapter}

object Bot {
  implicit val system = ActorSystem("scalagram")
  implicit val mat = ActorMaterializer()
  implicit val ec = system.dispatcher
}