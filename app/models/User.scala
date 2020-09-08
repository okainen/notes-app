package models

import java.time.Instant

import helpers.TimeHelper
import io.jvm.uuid.UUID
import models.dtos.UserDto


case class User(id: UUID,
                username: String,
                email: String,
                password: String,
                created: Instant,
                modified: Instant,
                active: Boolean = false) {
  def update(userDto: UserDto)(implicit timeHelper: TimeHelper): User = this.copy(
    username = userDto.username,
    email = userDto.email,
    password = userDto.password,
    modified = timeHelper.now()
  )

  def activate: User = this.copy(active = true)
}

object User {
  def tupled: ((UUID, String, String, String, Instant, Instant, Boolean)) => User = (User.apply _).tupled
}
