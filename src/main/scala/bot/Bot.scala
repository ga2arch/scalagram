package bot

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object Bot {
  implicit val system = ActorSystem("scalagram")
  implicit val mat = ActorMaterializer()
  implicit val ec = system.dispatcher

  def run(): Unit = {
    Server.run()
  }
}