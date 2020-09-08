package exceptions

import play.api.data.FormError


object Exceptions {

  case class NoJsonPassedException(message: String = "No JSON passed") extends RuntimeException(message)

  case class WrongUuidException(message: String = "Wrong UUID passed") extends RuntimeException(message)

  case class WrongJsonException(errors: Seq[FormError]) extends RuntimeException(errors.toString)

  case class WrongCredentialsException(message: String = "Wrong credentials passed")
    extends RuntimeException(message)

  case class UserAlreadyExistsException(message: String) extends RuntimeException(message)

  case class UserNotActiveException(message: String = "User is not active") extends RuntimeException(message)

  object BadRequestException extends RuntimeException("Bad Request")

  object UnauthorizedException extends RuntimeException("Unauthorized")

  object NotFoundException extends RuntimeException("Not Found")

  object ForbiddenException extends RuntimeException("Forbidden")

}
