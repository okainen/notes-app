package daos

import io.jvm.uuid.UUID
import models.Post
import monix.eval.Task

trait PostDao {
  def create(post: Post): Task[Unit]

  def get(id: UUID): Task[Option[Post]]

  def getAll(userId: UUID): Task[Seq[Post]]

  def update(post: Post): Task[Unit]

  def delete(id: UUID): Task[Unit]
}
