package testUtils

import io.jvm.uuid.UUID
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest

import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, Future}

trait TestUtils {

  val userId: UUID = UUID(0, 0)
  val anotherUserId: UUID = UUID(0, 1)
  val correctResourceId: UUID = UUID(0, 0)
  val incorrectResourceId: String = "abcd"

  implicit class AuthorizeRequest(request: FakeRequest[AnyContent]) {
    def authorized: FakeRequest[AnyContent] = request.withSession("userId" -> userId.toString)
  }

  implicit class AwaitFuture[T](future: Future[T]) {
    def await: T = Await.result(future, 5.seconds)
  }

  implicit class AwaitTask[T](task: Task[T]) {
    def await: T = task.runToFuture.await
  }

}
