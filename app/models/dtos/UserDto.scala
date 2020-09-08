package models.dtos

import helpers.{BCryptHelper, TimeHelper, UuidHelper}
import io.jvm.uuid.UUID
import models.User
import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.data.validation.Constraints.{emailAddress, minLength, pattern}


case class UserDto(username: String, email: String, password: String) {
  def toUser(implicit bcryptHelper: BCryptHelper, timeHelper: TimeHelper, uuidHelper: UuidHelper): User = {
    val now = timeHelper.now()
    User(UUID.randomUUID(), username, email, bcryptHelper.encrypt(password, 13), now, now)
  }
}

object UserDto {
  implicit val form: Form[UserDto] = Form(
    mapping(
      "username" -> text.verifying(minLength(1, errorMessage = "This field is required")),
      "email" -> text.verifying(emailAddress(errorMessage = "Wrong email format")),
      "password" -> text.verifying(pattern(
        regex = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,128}".r,
        name = "password",
        error =
          "Password should have 8 to 128 alphanumeric characters, at least one lowercase letter, one uppercase and one digit"
      ))
    )(UserDto.apply)(UserDto.unapply)
  )
}
