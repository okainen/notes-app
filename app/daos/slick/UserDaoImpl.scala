package daos.slick

import daos.UserDao
import io.jvm.uuid.UUID
import models.User
import monix.eval.Task
import monix.execution.Scheduler
import play.api.db.slick.HasDatabaseConfig
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile


class UserDaoImpl(dbConfig: DatabaseConfig[JdbcProfile])(implicit s: Scheduler)
  extends Tables(dbConfig) with UserDao with HasDatabaseConfig[JdbcProfile] {

  import daos.daoUtils.DaoUtils._
  import profile.api._

  def create(user: User): Task[Unit] = db.run(users += user).map(_ => ()).toTask

  def get(id: UUID): Task[Option[User]] = db.run(users.filter(_.id === id).result.headOption).toTask

  def getByEmail(email: String): Task[Option[User]] =
    db.run(users.filter(_.email === email).result.headOption).toTask

  def getByUsername(username: String): Task[Option[User]] =
    db.run(users.filter(_.username === username).result.headOption).toTask

  def update(user: User): Task[Unit] =
    db.run(users.filter(_.id === user.id).update(user)).map(_ => ()).toTask

  def delete(id: UUID): Task[Unit] = db.run(users.filter(_.id === id).delete).map(_ => ()).toTask

}