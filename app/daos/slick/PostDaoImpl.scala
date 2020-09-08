package daos.slick

import daos.PostDao
import io.jvm.uuid.UUID
import models.Post
import monix.eval.Task
import monix.execution.Scheduler
import play.api.db.slick.HasDatabaseConfig
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile


class PostDaoImpl(dbConfig: DatabaseConfig[JdbcProfile])(implicit s: Scheduler)
  extends Tables(dbConfig) with PostDao with HasDatabaseConfig[JdbcProfile] {

  import daos.daoUtils.DaoUtils._
  import profile.api._

  def create(post: Post): Task[Unit] = db.run(posts += post).map(_ => ()).toTask

  def get(id: UUID): Task[Option[Post]] = db.run(posts.filter(_.id === id).result.headOption).toTask

  def getAll(userId: UUID): Task[Seq[Post]] = db.run(posts.filter(_.userId === userId).result).toTask

  def update(post: Post): Task[Unit] =
    db.run(posts.filter(_.id === post.id).update(post)).map(_ => ()).toTask

  def delete(id: UUID): Task[Unit] =
    db.run(posts.filter(_.id === id).delete).map(_ => ()).toTask

}