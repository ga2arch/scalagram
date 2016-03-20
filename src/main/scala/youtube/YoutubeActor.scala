package youtube

import java.nio.file.Paths

import akka.actor.{Actor, ActorLogging, Props}
import com.pengrad.telegrambot.model.Chat
import org.apache.commons.io.FileUtils
import youtube.YoutubeActor.DownloadUrl

object YoutubeActor {
  case class DownloadUrl(chat: Chat, code: String)
}

class YoutubeActor extends Actor with ActorLogging {
  val worker = context.actorOf(Props[YoutubeWorker], "worker")

  override def preStart(): Unit = {
    super.preStart()

    val dlFolder = Paths.get("static")
    FileUtils.deleteDirectory(dlFolder.toFile)
  }

  override def receive: Receive = {
    case m@DownloadUrl(_, code) =>
      println("Downloading " + code)
      worker ! m
  }
}
