package models

import java.time.Instant
import java.util.UUID

import helpers.{TimeHelper, TokenHelper}

case class Token(userId: UUID,
                 body: String,
                 created: Instant) {
  def isExpired: Boolean = Instant.now().isAfter(created.plusSeconds(Token.ttl))
}

object Token {
  val ttl: Long = 43200

  def generate(userId: UUID)
              (implicit tokenHelper: TokenHelper, timeHelper: TimeHelper): Token =
    Token(
      userId,
      tokenHelper.generate(),
      timeHelper.now()
    )

  def tupled: ((UUID, String, Instant)) => Token = (Token.apply _).tupled
}
