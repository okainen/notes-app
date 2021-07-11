import _root_.controllers.{AssetsComponents, AuthController, PostController}
import com.typesafe.config.Config
import daos.mongo.PostDaoImpl
import daos.slick.{TokenDaoImpl, UserDaoImpl}
import daos.{PostDao, TokenDao, UserDao}
import monix.execution.Scheduler
import org.flywaydb.play.FlywayPlayComponents
import play.api.ApplicationLoader.Context
import play.api.db.slick.{DbName, SlickComponents}
import play.api.libs.mailer.MailerComponents
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.mvc.EssentialFilter
import play.api.routing.Router
import play.api.{Application, ApplicationLoader}
import play.filters.HttpFiltersComponents
import play.filters.cors.{CORSComponents, CORSFilter}
import play.modules.reactivemongo.ReactiveMongoApiFromContext
import router.Routes
import services.impl.{AuthServiceImpl, MailerServiceImpl, PostServiceImpl}
import services.{AuthService, MailerService, PostService}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

class NotesApiLoader extends ApplicationLoader {
  def load(context: Context): Application = {
    new AppComponents(context).application
  }
}

class AppComponents(context: Context) extends
  ReactiveMongoApiFromContext(context) with AhcWSComponents
  with AssetsComponents with HttpFiltersComponents with SlickComponents
  with FlywayPlayComponents with MailerComponents with CORSComponents {

  import com.softwaremill.macwire._

  flywayPlayInitializer

  lazy val config: Config = configuration.underlying
  lazy val prefix: String = "/"
  lazy val router: Router = wire[Routes]

  override lazy val httpFilters: Seq[EssentialFilter] = Seq(wire[CORSFilter])

  lazy val dbConfig: DatabaseConfig[JdbcProfile] = slickApi.dbConfig[JdbcProfile](DbName("notes"))

  implicit lazy val s: Scheduler = Scheduler.Implicits.global

  lazy val userDao: UserDao = wire[UserDaoImpl]
  lazy val tokenDao: TokenDao = wire[TokenDaoImpl]
  lazy val postDao: PostDao = wire[PostDaoImpl]

  lazy val mailerService: MailerService = wire[MailerServiceImpl]
  lazy val authService: AuthService = wire[AuthServiceImpl]
  lazy val postService: PostService = wire[PostServiceImpl]

  lazy val authController: AuthController = wire[AuthController]
  lazy val postController: PostController = wire[PostController]
}
