package helpers

import io.jvm.uuid.UUID


trait UuidHelper {
  def generate(): UUID
}

object UuidHelper {
  implicit val defaultUuidHelper: UuidHelper = () => UUID.randomUUID()
}
