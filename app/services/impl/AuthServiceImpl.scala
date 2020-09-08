package services.impl

import daos.{TokenDao, UserDao}
import exceptions.Exceptions._
import helpers.{BCryptHelper, TimeHelper, TokenHelper, UuidHelper}
import models.dtos.{CredentialsDto, UserDto}
import models.{Token, User}
import monix.eval.Task
import services.{AuthService, MailerService}


class AuthServiceImpl(userDao: UserDao,
                      tokenDao: TokenDao,
                      mailerService: MailerService)
  extends AuthService(userDao, tokenDao, mailerService) {
  def signup(userDto: UserDto)
            (implicit
             bcryptHelper: BCryptHelper,
             timeHelper: TimeHelper,
             tokenHelper: TokenHelper,
             uuidHelper: UuidHelper): Task[Unit] = {
    val user = userDto.toUser
    userDao.getByEmail(user.email).flatMap {
      case None =>
        userDao.getByUsername(user.username).flatMap {
          case None =>
            userDao.create(user).flatMap { _ =>
              createAndSendToken(user)
            }
          case _ => Task.raiseError(UserAlreadyExistsException("User with given username already exists"))
        }
      case _ => Task.raiseError(UserAlreadyExistsException("User with given email already exists"))
    }
  }

  def signin(credentialsDto: CredentialsDto)
            (implicit timeHelper: TimeHelper, tokenHelper: TokenHelper): Task[String] =
    withCorrectCredentials(credentialsDto).flatMap { user =>
      if (user.active) Task.now(user.id.toString)
      else tokenDao.getByUserId(user.id).flatMap {
        case Some(token) =>
          if (token.isExpired)
            tokenDao.delete(token.body).flatMap { _ =>
              createAndSendToken(user).flatMap { _ =>
                Task.raiseError(UserNotActiveException())
              }
            }
          else mailerService.sendMail(user.email, token.body).flatMap { _ =>
            Task.raiseError(UserNotActiveException())
          }
        case None => createAndSendToken(user).flatMap { _ =>
          Task.raiseError(UserNotActiveException())
        }
      }
    }

  def verify(tokenBody: String): Task[Unit] =
    tokenDao.get(tokenBody).flatMap {
      case Some(token) =>
        tokenDao.delete(tokenBody).flatMap { _ =>
          if (token.isExpired) Task.raiseError(NotFoundException)
          else userDao.get(token.userId).flatMap {
            case Some(user) => userDao.update(user.activate)
            case None => Task.raiseError(NotFoundException)
          }
        }
      case None => Task.raiseError(NotFoundException)
    }

  private def createAndSendToken(user: User)
                                (implicit timeHelper: TimeHelper, tokenHelper: TokenHelper): Task[Unit] = {
    val token = Token.generate(user.id)
    tokenDao.create(token).flatMap{_ =>
      mailerService.sendMail(user.email, token.body)
    }
  }

  private def withCorrectCredentials(credentialsDto: CredentialsDto): Task[User] =
    userDao.getByEmail(credentialsDto.email).flatMap {
      case Some(user) =>
        import com.github.t3hnar.bcrypt._
        if (credentialsDto.password.isBcrypted(user.password)) Task.now(user)
        else Task.raiseError(WrongCredentialsException())
      case None => Task.raiseError(WrongCredentialsException())
    }
}
