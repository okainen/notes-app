package helpers

import java.time.Instant


trait TimeHelper {
  def now(): Instant
}

object TimeHelper {
  implicit val defaultTokenHelper: TimeHelper = () => Instant.now()
}
