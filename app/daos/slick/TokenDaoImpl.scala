package daos.slick

import daos.TokenDao
import io.jvm.uuid.UUID
import models.Token
import monix.eval.Task
import monix.execution.Scheduler
import play.api.db.slick.HasDatabaseConfig
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

class TokenDaoImpl(dbConfig: DatabaseConfig[JdbcProfile])(implicit s: Scheduler)
  extends Tables(dbConfig) with TokenDao with HasDatabaseConfig[JdbcProfile] {

  import daos.daoUtils.DaoUtils._
  import profile.api._

  def create(token: Token): Task[Unit] = db.run(tokens += token).map(_ => ()).toTask

  def get(body: String): Task[Option[Token]] =
    db.run(tokens.filter(_.body === body).result.headOption).toTask

  def getByUserId(userId: UUID): Task[Option[Token]] =
    db.run(tokens.filter(_.userId === userId).result.headOption).toTask

  def delete(body: String): Task[Unit] =
    db.run(tokens.filter(_.body === body).delete).map(_ => ()).toTask
}
