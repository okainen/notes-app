package daos.slick

import java.time.Instant

import io.jvm.uuid.UUID
import models.{Post, Token, User}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile


class Tables(protected val dbConfig: DatabaseConfig[JdbcProfile]) {

  import dbConfig.profile.api._

  val users = TableQuery[Users]
  val tokens = TableQuery[Tokens]
  val posts = TableQuery[Posts]

  class Users(tag: Tag) extends Table[User](tag, "users") {
    def * = (id, username, email, password, created, modified, active).mapTo[User]

    def id = column[UUID]("id", O.PrimaryKey)

    def username = column[String]("username")

    def email = column[String]("email", O.Unique)

    def password = column[String]("password")

    def created = column[Instant]("created")

    def modified = column[Instant]("modified")

    def active = column[Boolean]("active")
  }

  class Tokens(tag: Tag) extends Table[Token](tag, "tokens") {
    lazy val usersFk = foreignKey("tokens_user_id_fkey", userId, users)(
      _.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade
    )

    def * = (userId, body, created).mapTo[Token]

    def body = column[String]("body", O.PrimaryKey)

    def userId = column[UUID]("user_id", O.Unique)

    def created = column[Instant]("created")
  }

  class Posts(tag: Tag) extends Table[Post](tag, "posts") {
    lazy val usersFk = foreignKey("posts_user_id_fkey", userId, users)(
      _.id, onUpdate = ForeignKeyAction.Cascade, onDelete = ForeignKeyAction.Cascade
    )

    def * = (id, userId, title, content, modified).mapTo[Post]

    def id = column[UUID]("id", O.PrimaryKey)

    def userId = column[UUID]("user_id")

    def title = column[String]("title")

    def content = column[String]("content")

    def modified = column[Instant]("modified")
  }

}
