package helpers

import io.jvm.uuid.UUID


trait TokenHelper {
  def generate(): String
}

object TokenHelper {
  implicit val defaultTokenHelper: TokenHelper = () => UUID.randomUUID().toString
}
