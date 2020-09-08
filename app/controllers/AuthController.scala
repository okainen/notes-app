package controllers

import controllers.controllerUtils.ControllerUtils
import models.dtos.{CredentialsDto, UserDto}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.AuthService


class AuthController(protected val authService: AuthService, cc: ControllerComponents)
  extends ControllerUtils(cc) {

  def signUp: Action[AnyContent] = actionWithJson[UserDto] { userDto =>
    authService.signup(userDto).map(_ => Created)
  }

  def signIn: Action[AnyContent] = actionWithJson[CredentialsDto] { credentialsDto =>
    authService.signin(credentialsDto).map(userId => Ok.withSession("userId" -> userId))
  }

  def signOut: Action[AnyContent] = userAction {
    monix.eval.Task.now(Ok.withNewSession)
  }

  def verify(token: String): Action[AnyContent] = simpleAction {
    authService.verify(token).map(_ => NoContent)
  }
}
