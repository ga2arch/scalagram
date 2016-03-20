import akka.actor.Props
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives._
import com.pengrad.telegrambot.BotUtils

object Server {
  import Bot._

  val botActor = system.actorOf(Props[BotActor], "bot")

  val route =
    pathSingleSlash {
      post {
        decodeRequest {
          entity(as[String]) { data =>
            complete {
              val update = BotUtils.parseUpdate(data)
              botActor ! update
              HttpResponse(200)
            }
          }
        }
      }
    }

  def run(): Unit = {
    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
  }

}
