package bot

import akka.actor.{Actor, ActorLogging, Props}
import com.pengrad.telegrambot.model.Update
import hackernews.HackernewsActor
import hackernews.HackernewsActor.GetTopStories
import youtube.YoutubeActor
import youtube.YoutubeActor.DownloadUrl

class BotActor extends Actor with ActorLogging {
  val hnActor = context.actorOf(Props[HackernewsActor], "hn")
  val youtubeActor = context.actorOf(Props[YoutubeActor], "youtube")
  val youtubePattern = "(?:youtube\\.com\\/\\S*(?:(?:\\/e(?:mbed))?\\/|watch\\?(?:\\S*?&?v\\=))|youtu\\.be\\/)([a-zA-Z0-9_-]{6,11})".r

  override def receive: Receive = {
    case u: Update => {
      log.info("Received: " + u.toString)
      if (u.message() != null && u.message().text() != null) {

        val text = u.message().text()

        text match {
          case "/hn" =>
            hnActor ! GetTopStories

          case _  =>
            val result = youtubePattern findAllIn text
            if (result.hasNext && result.groupCount > 0) {
              val code = result.group(1)
              youtubeActor ! DownloadUrl(u.message().chat(), code)
            }
        }
      }
    }
  }
}
