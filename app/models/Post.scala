package models

import java.time.Instant

import helpers.TimeHelper
import io.jvm.uuid.UUID
import models.dtos.PostDto
import play.api.libs.json.{JsValue, Json, Writes}
import reactivemongo.api.bson.{BSONDocument, BSONDocumentHandler}

import scala.util.Try


case class Post(id: UUID, userId: UUID, title: String, content: String, modified: Instant) {
  def toJson: JsValue = Json.toJson(this)

  def update(postDto: PostDto)(implicit timeHelper: TimeHelper): Post = this.copy(
    title = postDto.title,
    content = postDto.content,
    modified = timeHelper.now()
  )
}

object Post {
  implicit val writes: Writes[Post] = (post: Post) => {
    Json.obj(
      "id" -> post.id.toString,
      "title" -> post.title,
      "content" -> post.content,
      "modified" -> post.modified.toString
    )
  }

  implicit val handler: BSONDocumentHandler[Post] = new BSONDocumentHandler[Post] {
    override def readDocument(bson: BSONDocument): Try[Post] = Try {
      readOpt(bson).get
    }

    private def readOpt(bson: BSONDocument): Option[Post] = for {
      id <- bson.getAsOpt[String]("_id")
      userId <- bson.getAsOpt[String]("userId")
      title <- bson.getAsOpt[String]("title")
      content <- bson.getAsOpt[String]("content")
      modified <- bson.getAsOpt[Instant]("modified")
    } yield Post(UUID.fromString(id), UUID.fromString(userId), title, content, modified)

    override def writeTry(post: Post): Try[BSONDocument] = Try {
      BSONDocument(
        "_id" -> post.id,
        "userId" -> post.userId,
        "title" -> post.title,
        "content" -> post.content,
        "modified" -> post.modified
      )
    }
  }

  def tupled: ((UUID, UUID, String, String, Instant)) => Post = (Post.apply _).tupled
}