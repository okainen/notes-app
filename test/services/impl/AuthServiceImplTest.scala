package services.impl

import java.time.Instant

import daos.{TokenDao, UserDao}
import exceptions.Exceptions.{NotFoundException, UserAlreadyExistsException, UserNotActiveException, WrongCredentialsException}
import helpers.{BCryptHelper, TimeHelper, TokenHelper, UuidHelper}
import io.jvm.uuid.UUID
import models.dtos.{CredentialsDto, UserDto}
import models.{Token, User}
import monix.eval.Task
import org.scalamock.scalatest.MockFactory
import org.scalatestplus.play.PlaySpec
import services.MailerService
import testUtils.TestUtils


class AuthServiceImplTest extends PlaySpec with MockFactory with TestUtils {
  protected implicit val timeHelper: TimeHelper = () => Instant.MAX.minusSeconds(Token.ttl)
  protected implicit val tokenHelper: TokenHelper = () => "token"
  protected val uuidHelper: UuidHelper = () => UUID(0, 0)
  protected val userDaoMock: UserDao = mock[UserDao]
  protected val tokenDaoMock: TokenDao = mock[TokenDao]
  protected val mailerServiceMock: MailerService = mock[MailerService]
  protected val service = new AuthServiceImpl(userDaoMock, tokenDaoMock, mailerServiceMock)
  protected val userDto: UserDto = UserDto("username", "some.email@mail.com", "pasSw0rd")
  protected val userDtoMock: UserDto = mock[UserDto]
  protected val user: User = userDto.toUser
  protected val activeUser: User = user.activate
  protected val existingToken: Token = Token.generate(user.id)(tokenHelper, timeHelper)
  protected val expiredToken: Token = Token.generate(user.id)(tokenHelper, () => Instant.MIN.plusSeconds(Token.ttl))

  "signup" should {
    "create user, create and send token, return (): Unit" in {
      (userDtoMock.toUser(_: BCryptHelper, _: TimeHelper, _: UuidHelper))
        .expects(*, *, *) returns user
      (userDaoMock.getByEmail(_: String)) expects user.email returns Task.now(None)
      (userDaoMock.getByUsername(_: String)) expects user.username returns Task.now(None)
      (userDaoMock.create(_: User)) expects user returns Task.now(())
      (tokenDaoMock.create(_: Token)) expects existingToken returns Task.now(())
      (mailerServiceMock.sendMail(_: String, _: String))
        .expects(user.email, existingToken.body) returns Task.now(())
      service.signup(userDtoMock).await mustBe()
    }

    "raise UserAlreadyExists exception when user with given email already exists" in {
      (userDtoMock.toUser(_: BCryptHelper, _: TimeHelper, _: UuidHelper))
        .expects(*, *, *) returns user
      (userDaoMock.getByEmail(_: String)) expects user.email returns Task.now(Some(user))
      a[UserAlreadyExistsException] should be thrownBy service.signup(userDtoMock).await
    }

    "raise UserAlreadyExists exception when user with given username already exists" in {
      val differentEmail = "some.different@email.com"
      (userDtoMock.toUser(_: BCryptHelper, _: TimeHelper, _: UuidHelper))
        .expects(*, *, *) returns user.copy(email = differentEmail)
      (userDaoMock.getByEmail(_: String)) expects differentEmail returns Task.now(None)
      (userDaoMock.getByUsername(_: String)) expects user.username returns Task.now(Some(user))
      a[UserAlreadyExistsException] should be thrownBy service.signup(userDtoMock).await
    }
  }

  "signin" should {
    import com.github.t3hnar.bcrypt._
    val credentialsDto = CredentialsDto(userDto.email, userDto.password)

    "return Task(userId)" in {
      (userDaoMock.getByEmail(_: String)) expects credentialsDto.email returns Task.now(Some(activeUser))
      service.signin(credentialsDto).await mustBe activeUser.id.toString
    }

    "raise WrongCredentialsException if user with given email is absent in db" in {
      val wrongEmail = "some.wrong@email.com"
      (userDaoMock.getByEmail(_: String)) expects wrongEmail returns Task.now(None)
      a[WrongCredentialsException] should be thrownBy service.signin(credentialsDto.copy(email = wrongEmail)).await
    }

    "raise WrongCredentialsException if wrong password passed" in {
      val wrongPassword = "wrong_password"
      (userDaoMock.getByEmail(_: String)) expects credentialsDto.email returns Task.now(Some(activeUser))
      a[WrongCredentialsException] should be thrownBy service.signin(
        credentialsDto.copy(password = wrongPassword.bcrypt)
      ).await
    }

    "create and send token, raise UserNotActiveException if user is not active and not owning a token" in {
      (userDaoMock.getByEmail(_: String)) expects credentialsDto.email returns Task.now(Some(user))
      (tokenDaoMock.getByUserId(_: UUID)) expects user.id returns Task.now(None)
      (tokenDaoMock.create(_: Token)) expects existingToken returns Task.now(())
      (mailerServiceMock.sendMail(_: String, _: String))
        .expects(user.email, existingToken.body) returns Task.now(())
      a[UserNotActiveException] should be thrownBy service.signin(credentialsDto).await
    }

    "delete expired token; create and send new token; raise UserNotActiveException if user is not active" in {
      (userDaoMock.getByEmail(_: String)) expects credentialsDto.email returns Task.now(Some(user))
      (tokenDaoMock.getByUserId(_: UUID)) expects user.id returns Task.now(Some(expiredToken))
      (tokenDaoMock.delete(_: String)) expects expiredToken.body returns Task.now(())
      (tokenDaoMock.create(_: Token)) expects existingToken returns Task.now(())
      (mailerServiceMock.sendMail(_: String, _: String))
        .expects(user.email, existingToken.body) returns Task.now(())
      a[UserNotActiveException] should be thrownBy service.signin(credentialsDto).await
    }

    "resend existing token and raise UserNotActiveException if user is not active" in {
      (userDaoMock.getByEmail(_: String)) expects credentialsDto.email returns Task.now(Some(user))
      (tokenDaoMock.getByUserId(_: UUID)) expects user.id returns Task.now(Some(existingToken))
      (mailerServiceMock.sendMail(_: String, _: String))
        .expects(user.email, existingToken.body) returns Task.now(())
      a[UserNotActiveException] should be thrownBy service.signin(credentialsDto).await
    }
  }

  "verify" should {
    "activate user" in {
      (tokenDaoMock.get(_: String)) expects existingToken.body returns Task.now(Some(existingToken))
      (tokenDaoMock.delete(_: String)) expects existingToken.body returns Task.now(())
      (userDaoMock.get(_: UUID)) expects existingToken.userId returns Task.now(Some(user))
      (userDaoMock.update(_: User)) expects activeUser returns Task.now(())
      service.verify(existingToken.body).await mustBe()
    }

    "raise NotFoundException if token with given body is absent in db" in {
      (tokenDaoMock.get(_: String)) expects existingToken.body returns Task.now(None)
      a[NotFoundException.type] should be thrownBy service.verify(existingToken.body).await
    }

    "delete token with given body and raise NotFoundException if there is no user having the token" in {
      (tokenDaoMock.get(_: String)) expects existingToken.body returns Task.now(Some(existingToken))
      (tokenDaoMock.delete(_: String)) expects existingToken.body returns Task.now(())
      (userDaoMock.get(_: UUID)) expects existingToken.userId returns Task.now(None)
      a[NotFoundException.type] should be thrownBy service.verify(existingToken.body).await
    }

    "delete token with given body and raise NotFoundException if the token is expired" in {
      (tokenDaoMock.get(_: String)) expects expiredToken.body returns Task.now(Some(expiredToken))
      (tokenDaoMock.delete(_: String)) expects expiredToken.body returns Task.now(())
      a[NotFoundException.type] should be thrownBy service.verify(expiredToken.body).await
    }
  }
}
