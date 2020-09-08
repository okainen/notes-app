package daos.mongo

import daos.PostDao
import io.jvm.uuid.UUID
import models.Post
import monix.eval.Task
import monix.execution.Scheduler
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.BSONDocument
import reactivemongo.api.bson.collection.BSONCollection

class PostDaoImpl(val reactiveMongoApi: ReactiveMongoApi)(implicit s: Scheduler) extends PostDao {

  import daos.daoUtils.DaoUtils._

  private def posts: Task[BSONCollection] =
    reactiveMongoApi.database.map(_.collection("posts")).toTask

  def create(post: Post): Task[Unit] = posts.flatMap(_.insert.one(post).toTask).map(_ => ())

  def get(id: UUID): Task[Option[Post]] = posts.flatMap {
    _.find(BSONDocument("_id" -> id)).one[Post].toTask
  }

  def getAll(userId: UUID): Task[Seq[Post]] = posts.flatMap {
    _.find(BSONDocument("userId" -> userId)).cursor[Post]().collect[List]().toTask
  }

  def update(post: Post): Task[Unit] = posts.flatMap {
    _.update.one(
      q = BSONDocument("_id" -> post.id),
      u = BSONDocument("$set" -> post),
      upsert = false,
      multi = false
    ).toTask.map(_ => ())
  }

  def delete(id: UUID): Task[Unit] = posts.flatMap {
    _.delete.one(BSONDocument("_id" -> id)).toTask.map(_ => ())
  }
}
