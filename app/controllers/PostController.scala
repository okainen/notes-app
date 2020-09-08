package controllers

import controllers.controllerUtils.ControllerUtils
import exceptions.Exceptions.WrongUuidException
import io.jvm.uuid.UUID
import models.dtos.PostDto
import monix.eval.Task
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.PostService

import scala.util.{Success, Try}


class PostController(protected val postsService: PostService, cc: ControllerComponents)
  extends ControllerUtils(cc) {

  def create: Action[AnyContent] = userActionWithJson[PostDto] { (userId, postDto) =>
    postsService.create(userId, postDto).map(post => Created(post.toJson))
  }

  def get(strId: String): Action[AnyContent] = userAction { userId =>
    withValidUuid(strId).flatMap(id => postsService.get(userId, id)).map(post => Ok(post.toJson))
  }

  def getAll: Action[AnyContent] = userAction { userId =>
    postsService.getAll(userId).map { posts =>
      Json.toJson(posts.map(post => post.toJson))
    }.map(posts => Ok(Json.obj("posts" -> posts)))
  }

  def update(strId: String): Action[AnyContent] = userActionWithJson[PostDto] { (userId, postDto) =>
    withValidUuid(strId).flatMap(id => postsService.update(userId, id, postDto)).map(post => Ok(post.toJson))
  }

  def delete(strId: String): Action[AnyContent] = userAction { userId =>
    withValidUuid(strId).flatMap(id => postsService.delete(userId, id)).map(_ => NoContent)
  }

  private def withValidUuid(id: String): Task[UUID] = Try(UUID.fromString(id)) match {
    case Success(validUuid) => Task.now(validUuid)
    case _ => Task.raiseError(WrongUuidException())
  }
}
