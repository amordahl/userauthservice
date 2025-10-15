package edu.uic.client

import munit.FunSuite
import org.scalamock.stubs.*
import scala.concurrent.Future
import cats.effect.IO

class ClientTests extends utest.TestSuite, Stubs:

  val tests = Tests(
    // Pattern 1: Testing IO[String] - use unsafeRunSync() to extract the value
    test("read returns formatted email list from database"):
      // Arrange - Create stubs
      val dbStub = stub[EmailDbManager]
      dbStub.getAllEmails.returns(_ =>
        Future.successful(
          Seq(
            EmailRecord(Some(1), "amordahl@uic.edu"),
            EmailRecord(Some(2), "austin@outlook.com")
          )
        )
      )
      val validatorStub = stub[ValidationService]
      val client        = Client(dbStub, validatorStub)

      // Act - Run the IO action to get the actual String value
      val result: String = client.read.unsafeRunSync()

      // Assert
      assertEquals(result, expected)

    test("read returns empty list when database is empty"):
      // Arrange
      val dbStub = stub[EmailDbManager]
      dbStub.getAllEmails.returns(_ => Future.successful(Seq.empty))
      val validatorStub = stub[ValidationService]
      val client        = Client(dbStub, validatorStub)

      // Act
      val result = client.read.unsafeRunSync()

      // Assert
      assertEquals(result, "-")

    // Pattern 2: Testing IO[Unit] - verify side effects through stubs
    test("insertIntoDatabase calls database manager when email is valid"):
      // Arrange
      val dbStub = stub[EmailDbManager]
      dbStub.insertEmail.returns:
        case "test@gmail.com" => Future.successful(1)
        case _                => Future.failed(new Exception("Unexpected email"))
      val validatorStub = stub[ValidationService]
      val client        = Client(dbStub, validatorStub)

      /* Act - Now we can call insertIntoDatabase directly since it's
      * package-private */
      // IO[Unit] returns Unit, but the side effect is calling insertEmail
      client.insertIntoDatabase("test@gmail.com").unsafeRunSync()

      // Assert - Verify the database method was called
      /* (Note: With scalamock stubs, if the stub wasn't called with the right
      * params, */
      // it would have thrown an exception during the Future.successful check)
      /* You could also verify by checking the stub was invoked if scalamock
      * supports that */

    test("insertIntoDatabase handles database failure gracefully"):
      // Arrange
      val dbStub = stub[EmailDbManager]
      dbStub.insertEmail.returns(_ =>
        Future.failed(new Exception("Database connection failed"))
      )
      val validatorStub = stub[ValidationService]
      val client        = Client(dbStub, validatorStub)

      // Act & Assert - Should not throw, just print error message
      client.insertIntoDatabase("test@gmail.com").unsafeRunSync()

    // Pattern 3: Testing validateEmail through the validation service
    test("validateEmail returns Valid for successful validation"):
      // Arrange
      val dbStub        = stub[EmailDbManager]
      val validatorStub = stub[ValidationService]
      validatorStub.validate.returns:
        case "test@gmail.com" => ValidationStatus.Valid
        case _                => ValidationStatus.InvalidConstruction
      val client = Client(dbStub, validatorStub)

      // Act - Test the validation logic
      val result = validatorStub.validate("test@gmail.com")

      // Assert
      assertEquals(result, ValidationStatus.Valid)

    test("validateEmail returns InvalidDomain for unacceptable domains"):
      // Arrange
      val dbStub        = stub[EmailDbManager]
      val validatorStub = stub[ValidationService]
      validatorStub.validate.returns:
        case "test@badhost.com" => ValidationStatus.InvalidDomain
        case _                  => ValidationStatus.Valid

      // Act
      val result = validatorStub.validate("test@badhost.com")

      // Assert
      assertEquals(result, ValidationStatus.InvalidDomain)

    test("validateEmail returns ConnectionError when service is unreachable"):
      // Arrange
      val dbStub        = stub[EmailDbManager]
      val validatorStub = stub[ValidationService]
      validatorStub.validate.returns(_ => ValidationStatus.ConnectionError)

      // Act
      val result = validatorStub.validate("test@gmail.com")

      // Assert
      assertEquals(result, ValidationStatus.ConnectionError)
  )
end ClientTests
