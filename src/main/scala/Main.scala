

object Main extends App {
  override def main(args: Array[String]) {
    Server.run()
    Thread.currentThread().join()
  }
}
