package youtube

import java.nio.file.{Paths, Files}
import java.util.UUID

import akka.actor.{Actor, ActorLogging}
import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.request.InputFile
import org.apache.commons.io.FileUtils
import traits.BotTrait
import youtube.YoutubeActor.DownloadUrl
import scala.sys.process._

class InvalidCodeException(message: String) extends RuntimeException(message)
class DownloadErrorExcepetion(message: String) extends RuntimeException(message)

class YoutubeWorker extends Actor with ActorLogging with BotTrait {

  override def receive: Receive = {
    case DownloadUrl(chat, code) => {
      try {
        val title = getTitle(code)
        bot.sendMessage(chat.id(), title)
        download(chat, title, code)
      } catch {
        case e: InvalidCodeException => bot.sendMessage(chat.id(), e.getMessage)
      }
    }
  }

  def getTitle(code: String): String = {
    val buffer = new StringBuffer()
    val cmd = "youtube-dl -o %(title)s.%(ext)s --get-filename " + code
    val filename = cmd lineStream_! ProcessLogger(buffer append _)
    val stderr = buffer.toString

    if (stderr.isEmpty) {
      filename.head.substring(0, filename.head.lastIndexOf('.'))
    } else {
      val stderr = buffer.toString
      val error = stderr.substring(0, stderr.indexOf('.'))
      throw new InvalidCodeException(error)
    }
  }

  def download(chat: Chat, title: String, code: String): Unit = {
    val buffer = new StringBuffer()
    val dlPath = Files.createDirectories(Paths.get("static", UUID.randomUUID().toString))

    try {
      val cmd = Process("youtube-dl -o %(title)s.%(ext)s -x " +
        "-f bestaudio[filesize<50M][acodec!=opus]/bestaudio[filesize<50M] " + code, dlPath.toFile)

      cmd !! ProcessLogger(buffer append _)

      if (buffer.length() > 0) {
        throw new DownloadErrorExcepetion(buffer.toString)
      } else {
        val files = dlPath.toFile.listFiles
        if (files.nonEmpty) {
          val name = files.head.getName
          val path = dlPath.resolve(name)

          if (name.endsWith(".opus"))
            bot.sendAudio(chat.id(),
              InputFile.audio(path.toFile),
              null, null, title, null, null);
          else
            bot.sendAudio(chat.id(),
              InputFile.audio(path.toFile),
              null, null, title, null, null);

        } else {
          throw new DownloadErrorExcepetion("File not found")
        }
      }
    } finally {
      FileUtils.deleteDirectory(dlPath.toFile)
    }
  }
}
