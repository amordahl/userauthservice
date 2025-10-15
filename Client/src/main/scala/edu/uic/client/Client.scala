package edu.uic.client

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class Client(
    emailDbManager: EmailDbManager,
    validationService: ValidationService
):

  private def insertIntoDatabase(email: String): IO[Unit] =
    for
      _ <- IO.println("Email validated! Adding to database.")
      _ <- IO.pure(addToDatabase(email)).flatMap:
        case true =>
          IO.println("Email successfully added to database!")
        case false =>
          IO.println("Email could not be added to database.")
    yield ()

  private def emailPrompt: IO[Unit] =
    for
      _     <- IO.print("email> ")
      email <- IO.readLine
      _ <- IO.pure(validateEmail(email)).flatMap:
        case true => insertIntoDatabase(email)
        case false =>
          for
            _ <- IO.println("Email could not be validated.")
          yield ()
    yield ()

  private def help: IO[Unit] =
    for
      _ <- IO.println("The following commands are available.")
      _ <- IO.println("| help:   Print this help")
      _ <- IO.println("| insert: Validate and insert an email address")
      _ <- IO.println("| read:   Get all emails from the database")
    yield ()

  private def read: IO[Unit] =
    for
      _ <- IO.println("Not yet implemented")
    yield ()

  private def mainPrompt: IO[Unit] =
    for
      _ <- IO.print("> ")
      _ <- IO.readLine.flatMap:
        case "help"   => help
        case "insert" => emailPrompt
        case "read"   => read
        case _ =>
          for
            _ <- IO.println("Invalid response! Try again")
            _ <- mainPrompt
          yield ()
    yield ()

  private def validateEmail(email: String): Boolean = true

  private def addToDatabase(email: String): Boolean =
    val f = emailDbManager.insertEmail(email)
    Await.result(f.map(_ => true).recover(_ => false), 2.seconds)

  def main() =
    def loop: IO[Unit] =
      for
        _ <- mainPrompt
        _ <- loop
      yield ()

    val main =
      for
        _ <- IO.println(
          "Welcome to email processor app! Type 'help' for help."
        )
        _ <- loop
      yield ()

    main.unsafeRunSync()
  end main
end Client

@main
def entrypoint =
  val c = Client(
    EmailDbManager(
      slick.jdbc.PostgresProfile.api.Database.forConfig("emails")
    ),
    ValidationService()
  )
  c.main()
end entrypoint
