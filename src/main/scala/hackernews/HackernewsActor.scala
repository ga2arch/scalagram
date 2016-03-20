package hackernews

import java.util

import akka.actor.{ActorLogging, Actor}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.LinkParams.title
import akka.http.scaladsl.model.{HttpResponse, StatusCodes, StatusCode, HttpRequest}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.ByteString
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import hackernews.HackernewsActor.{TopStories, GetTopStories}

import scala.collection.mutable
import scala.concurrent.{Promise, Future}

case class HNStory(id: Long, title: String, url: String)

object HackernewsActor {
  case class GetTopStories()
  case class TopStories(stories: Seq[HNStory])
}

class HackernewsActor extends Actor with ActorLogging {
  import context.dispatcher
  import scala.collection.JavaConversions._

  final val baseUrl = "https://hacker-news.firebaseio.com/v0/"
  final val topUrl = baseUrl + "topstories.json"
  final val itemUrl = baseUrl + "item/"

  final implicit val mat: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))
  val http = Http(context.system)

  override def receive: Receive = {
    case GetTopStories => fetchStories()
  }

  private def fetchStories(): Unit = {
    call(topUrl, { content =>
      val stories: util.ArrayList[java.lang.Long] = new Gson().fromJson(content,
        new TypeToken[util.ArrayList[java.lang.Long]]() {}.getType)

      Future.sequence(stories.take(10).map((i) => fetchStory(i))).onSuccess {
        case s: mutable.Buffer[HNStory] => sender() ! TopStories(s.toSeq)
      }
    })
  }

  private def fetchStory(id: Long): Future[HNStory] = {
    call(itemUrl + id + ".json", { content =>
      val story = new Gson().fromJson(content, classOf[HNStory])
      story
    })
  }

  private def call[T](url: String, func: (String) => T): Future[T] = {
    val p = Promise[T]
    val f = http.singleRequest(HttpRequest(uri = url))

    f onSuccess {
      case HttpResponse(StatusCodes.OK, headers, entity, _) =>
        val resp = entity.dataBytes.runFold(ByteString(""))(_ ++ _)
        resp onSuccess {
          case bytestring => p.success(func(bytestring.utf8String))
        }

      case HttpResponse(code, _, _, _) =>
        log.info("Request failed, response code: " + code)
    }

    p.future
  }
}
