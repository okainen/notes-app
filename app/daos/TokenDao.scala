package daos

import io.jvm.uuid.UUID
import models.Token
import monix.eval.Task

trait TokenDao {
  def create(token: Token): Task[Unit]

  def get(body: String): Task[Option[Token]]

  def getByUserId(userId: UUID): Task[Option[Token]]

  def delete(body: String): Task[Unit]
}
