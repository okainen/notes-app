package controllers

import exceptions.Exceptions._
import helpers.{BCryptHelper, TimeHelper, TokenHelper, UuidHelper}
import io.jvm.uuid.UUID
import models.dtos.{CredentialsDto, UserDto}
import monix.eval.Task
import org.scalamock.scalatest.MockFactory
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.mvc.Results._
import play.api.test.FakeRequest
import play.api.test.Helpers.stubControllerComponents
import services.AuthService
import testUtils.TestUtils

class AuthControllerTest extends PlaySpec with MockFactory with TestUtils {
  val serviceMock: AuthService = mock[AuthService]
  val controller = new AuthController(serviceMock, stubControllerComponents())

  "signup" should {
    def fakeRequest(username: String, email: String, password: String): FakeRequest[AnyContent] = {
      val jsonBody = Json.obj(
        "username" -> username,
        "email" -> email,
        "password" -> password
      )
      FakeRequest().withBody(AnyContent(jsonBody))
    }

    val correctRequest: FakeRequest[AnyContent] =
      fakeRequest("john.doe", "john.doe@mail.com", "pasSw0rd")
    val correctDto = UserDto.form.bindFromRequest()(correctRequest).value.get
    val badRequest: FakeRequest[AnyContent] =
      fakeRequest("", "john.doemail.com", "pass")
    val formErrors = UserDto.form.bindFromRequest()(badRequest).errors

    "return Created" in {
      (serviceMock.signup(_: UserDto)(_: BCryptHelper, _: TimeHelper, _: TokenHelper, _: UuidHelper))
        .expects(correctDto, *, *, *, *) returns Task.now(())
      controller.signup(correctRequest).await mustBe Created
    }

    "return Conflict(User with given email already exists)" in {
      (serviceMock.signup(_: UserDto)(_: BCryptHelper, _: TimeHelper, _: TokenHelper, _: UuidHelper))
        .expects(correctDto, *, *, *, *) returns
        Task.raiseError(UserAlreadyExistsException("User with given email already exists"))
      controller.signup(correctRequest).await mustBe
        Conflict(Json.obj("message" -> "User with given email already exists"))
    }

    "return Conflict(User with given username already exists)" in {
      (serviceMock.signup(_: UserDto)(_: BCryptHelper, _: TimeHelper, _: TokenHelper, _: UuidHelper))
        .expects(correctDto, *, *, *, *) returns
        Task.raiseError(UserAlreadyExistsException("User with given username already exists"))
      controller.signup(correctRequest).await mustBe
        Conflict(Json.obj("message" -> "User with given username already exists"))
    }

    "return BadRequest(errors)" in {
      controller.signup(badRequest).await mustBe
        BadRequest(Json.obj(
          "errors" -> formErrors.map(e => Json.obj(e.key -> e.message))
        ))
    }
  }

  "signin" should {
    def fakeRequest(email: String, password: String): FakeRequest[AnyContent] = {
      val jsonBody = Json.obj(
        "email" -> email,
        "password" -> password
      )
      FakeRequest().withBody(AnyContent(jsonBody))
    }

    val correctRequest: FakeRequest[AnyContent] =
      fakeRequest("john.doe@mail.com", "pasSw0rd")
    val correctDto = CredentialsDto.form.bindFromRequest()(correctRequest).value.get
    val badRequest: FakeRequest[AnyContent] =
      fakeRequest("john.doemail.com", "pass")
    val formErrors = CredentialsDto.form.bindFromRequest()(badRequest).errors

    "return Ok with session containing userId" in {
      val userId = UUID.randomUUID().toString
      (serviceMock.signin(_: CredentialsDto)(_: TimeHelper, _: TokenHelper))
        .expects(correctDto, *, *) returns Task.now(userId)
      controller.signin(correctRequest).await mustBe Ok.withSession("userId" -> userId)
    }

    "return Unauthorized(Wrong credentials passed)" in {
      (serviceMock.signin(_: CredentialsDto)(_: TimeHelper, _: TokenHelper))
        .expects(correctDto, *, *) returns Task.raiseError(WrongCredentialsException())
      controller.signin(correctRequest).await mustBe
        Unauthorized(Json.obj("message" -> "Wrong credentials passed"))
    }

    "return Forbidden(User is not active)" in {
      (serviceMock.signin(_: CredentialsDto)(_: TimeHelper, _: TokenHelper))
        .expects(correctDto, *, *) returns Task.raiseError(UserNotActiveException())
      controller.signin(correctRequest).await mustBe
        Forbidden(Json.obj("message" -> "User is not active"))
    }

    "return BadRequest(errors)" in {
      controller.signin(badRequest).await mustBe
        BadRequest(Json.obj(
          "errors" -> formErrors.map(e => Json.obj(e.key -> e.message))
        ))
    }
  }

  "signout" should {
    "return Ok with new session" in {
      controller.signout(FakeRequest()).await mustBe Ok.withNewSession
    }
  }

  "verify" should {
    val someString = "string"

    "return NoContent" in {
      (serviceMock.verify(_: String)).expects(someString) returns Task.now(())
      controller.verify(someString)(FakeRequest()).await mustBe NoContent
    }

    "return NotFound" in {
      (serviceMock.verify(_: String)).expects(someString) returns Task.raiseError(NotFoundException)
      controller.verify(someString)(FakeRequest()).await mustBe NotFound
    }
  }
}
