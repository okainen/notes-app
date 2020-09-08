package services

import monix.eval.Task
import play.api.Configuration
import play.api.libs.mailer.MailerClient

abstract class MailerService(protected val mailerClient: MailerClient, protected val config: Configuration) {
  def sendMail(to: String, token: String): Task[Unit]
}
