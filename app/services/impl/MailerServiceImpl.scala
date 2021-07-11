package services.impl

import monix.eval.Task
import play.api.Configuration
import play.api.libs.mailer.{Email, MailerClient}
import services.MailerService

class MailerServiceImpl(mailerClient: MailerClient, config: Configuration)
  extends MailerService(mailerClient, config) {
  def sendMail(to: String, token: String): Task[Unit] = {
    val email = Email(
      subject = "notes-api email verification",
      from = s"notes-api <${config.get[String]("play.mailer.user")}>",
      to = Seq(to),
      bodyHtml = Some(
        s"<html><body><p>Please go by <a href='${config.get[String]("hostname")}/api/auth/verify/$token'>this link</a> to activate your account.</html>"
      )
    )
    Task(mailerClient send email).map(_ => ())
  }
}
