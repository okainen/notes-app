package models.dtos

import helpers.{TimeHelper, UuidHelper}
import io.jvm.uuid.UUID
import models.Post
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}


case class PostDto(title: String, content: String) {
  def toPost(userId: UUID)(implicit timeHelper: TimeHelper, uuidHelper: UuidHelper): Post =
    Post(uuidHelper.generate(), userId, title, content, timeHelper.now())
}

object PostDto {
  implicit val form: Form[PostDto] = Form(mapping(
    "title" -> nonEmptyText(1, 64),
    "content" -> nonEmptyText
  )(PostDto.apply)(PostDto.unapply))
}
