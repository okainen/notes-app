package models.dtos

import play.api.data.Form
import play.api.data.Forms.{mapping, text}
import play.api.data.validation.Constraints.{emailAddress, minLength}


case class CredentialsDto(email: String, password: String)

object CredentialsDto {
  implicit val form: Form[CredentialsDto] = Form(mapping(
    "email" -> text.verifying(emailAddress(errorMessage = "Wrong email format")),
    "password" -> text.verifying(minLength(1, errorMessage = "This field is required"))
  )(CredentialsDto.apply)(CredentialsDto.unapply))
}
