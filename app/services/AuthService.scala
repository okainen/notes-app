package services

import daos.{TokenDao, UserDao}
import helpers.{BCryptHelper, TimeHelper, TokenHelper, UuidHelper}
import models.dtos.{CredentialsDto, UserDto}
import monix.eval.Task


abstract class AuthService(protected val userDao: UserDao,
                           protected val tokenDao: TokenDao,
                           protected val mailerService: MailerService) {
  def signup(userDto: UserDto)
            (implicit
             bcryptHelper: BCryptHelper,
             timeHelper: TimeHelper,
             tokenHelper: TokenHelper,
             uuidHelper: UuidHelper): Task[Unit]

  def signin(credentialsDto: CredentialsDto)
            (implicit timeHelper: TimeHelper, tokenHelper: TokenHelper): Task[String]

  def verify(tokenBody: String): Task[Unit]
}
