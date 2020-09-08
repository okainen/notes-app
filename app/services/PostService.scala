package services

import daos.PostDao
import helpers.{TimeHelper, UuidHelper}
import io.jvm.uuid.UUID
import models.Post
import models.dtos.PostDto
import monix.eval.Task


abstract class PostService(protected val postDao: PostDao) {
  def create(userId: UUID, postDto: PostDto)(implicit uuidHelper: UuidHelper): Task[Post]

  def get(userId: UUID, id: UUID): Task[Post]

  def getAll(userId: UUID): Task[Seq[Post]]

  def update(userId: UUID, id: UUID, postDto: PostDto)(implicit timeHelper: TimeHelper): Task[Post]

  def delete(userId: UUID, id: UUID): Task[Unit]
}
