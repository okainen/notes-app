package controllers.controllerUtils

import exceptions.Exceptions._
import io.jvm.uuid.UUID
import monix.eval.Task
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._


abstract class ControllerUtils(cc: ControllerComponents) extends AbstractController(cc) {
  protected def simpleAction(block: Request[AnyContent] => Task[Result]): Action[AnyContent] =
    Action.async { implicit request =>
      import monix.execution.Scheduler.Implicits.global
      block(request).onErrorRecover(recover).runToFuture
    }
  
  protected def simpleAction(block: => Task[Result]): Action[AnyContent] = 
    Action.async {
      import monix.execution.Scheduler.Implicits.global
      block.onErrorRecover(recover).runToFuture
    }

  protected def actionWithJson[A](block: A => Task[Result])(implicit form: Form[A]): Action[AnyContent] =
    simpleAction { implicit request =>
      withValidJson[A].flatMap(block)
    }

  protected def userAction(block: => Task[Result]): Action[AnyContent] =
    simpleAction { implicit request =>
      withAuthority.flatMap(_ => block)
    }

  protected def userAction(block: UUID => Task[Result]): Action[AnyContent] =
    simpleAction { implicit request =>
      withAuthority.flatMap(block)
    }

  protected def userActionWithJson[A](block: (UUID, A) => Task[Result])(implicit form: Form[A]): Action[AnyContent] =
    simpleAction { implicit request =>
      withAuthority.flatMap { userId =>
        withValidJson[A].flatMap(dto => block(userId, dto))
      }
    }

  private def withAuthority(implicit request: Request[AnyContent]): Task[UUID] = request.session.get("userId") match {
    case Some(userId) => Task.now(UUID.fromString(userId))
    case None => Task.raiseError(UnauthorizedException)
  }

  private def withValidJson[A](implicit request: Request[AnyContent], form: Form[A]): Task[A] = {
    form.bindFromRequest().fold(
      formWithErrors => Task.raiseError(WrongJsonException(formWithErrors.errors)),
      dto => Task.now(dto)
    )
  }

  private def recover: PartialFunction[Throwable, Result] = {
    case BadRequestException => BadRequest
    case NoJsonPassedException(message) => BadRequest(Json.obj("message" -> message))
    case WrongUuidException(_) => NotFound
    case WrongJsonException(errors) => BadRequest(Json.obj(
      "errors" -> errors.map(e => Json.obj(e.key -> e.message))
    ))
    case UnauthorizedException => Unauthorized
    case WrongCredentialsException(message) => Unauthorized(Json.obj("message" -> message))
    case ForbiddenException => Forbidden
    case UserNotActiveException(message) => Forbidden(Json.obj("message" -> message))
    case NotFoundException => NotFound
    case UserAlreadyExistsException(message) => Conflict(Json.obj("message" -> message))
    case e =>
      e.printStackTrace()
      InternalServerError
  }
}
