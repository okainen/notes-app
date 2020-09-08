package daos

import io.jvm.uuid.UUID
import models.User
import monix.eval.Task

trait UserDao {
  def create(user: User): Task[Unit]

  def get(id: UUID): Task[Option[User]]

  def getByEmail(email: String): Task[Option[User]]

  def getByUsername(username: String): Task[Option[User]]

  def update(user: User): Task[Unit]

  def delete(id: UUID): Task[Unit]
}
