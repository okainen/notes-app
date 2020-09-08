package daos.daoUtils

import monix.eval.Task

import scala.concurrent.Future

object DaoUtils {

  implicit class FutureToTask[A](f: Future[A]) {
    def toTask: Task[A] = Task.fromFuture(f)
  }

}
