package edu.uic.client

import org.scalamock.stubs.*
import scala.concurrent.Future
import cats.effect.IO
import utest.*
import cats.effect.unsafe.implicits.global

class ClientTests extends utest.TestSuite, Stubs:

  val tests = Tests:
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
      assertGoldenLiteral(
        result,
        """-amordahl@uic.edu
        |-austin@outlook.com""".stripMargin
      )

    test("read returns empty list when database is empty"):
      // Arrange
      val dbStub = stub[EmailDbManager]
      dbStub.getAllEmails.returns(_ => Future.successful(Seq.empty))
      val validatorStub = stub[ValidationService]
      val client        = Client(dbStub, validatorStub)

      // Act
      val result = client.read.unsafeRunSync()

      // Assert
      assertGoldenLiteral(result, ())

    /* Pattern 2: Testing IO[Boolean] - verify database insertion without
     * console output */
    test("addToDatabase calls database manager when email is valid"):
      // Arrange
      val dbStub = stub[EmailDbManager]
      dbStub.insertEmail.returns:
        case "test@gmail.com" => Future.successful(1)
        case _ => Future.failed(new Exception("Unexpected email"))
      val validatorStub = stub[ValidationService]
      val client        = Client(dbStub, validatorStub)

      // Act - Call addToDatabase which doesn't print to console
      val result = client.addToDatabase("test@gmail.com").unsafeRunSync()

      // Assert - Verify the database insertion was successful
      result ==> true

    test("addToDatabase handles database failure gracefully"):
      // Arrange
      val dbStub = stub[EmailDbManager]
      dbStub.insertEmail.returns(_ =>
        Future.failed(new Exception("Database connection failed"))
      )
      val validatorStub = stub[ValidationService]
      val client        = Client(dbStub, validatorStub)

      // Act - addToDatabase returns false when insertion fails
      val result = client.addToDatabase("test@gmail.com").unsafeRunSync()

      // Assert - Verify failure is handled and returns false
      result ==> false

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
      val result = client.validateEmail("test@gmail.com").unsafeRunSync()

      // Assert
      result ==> ValidationStatus.Valid

    test("validateEmail returns InvalidDomain for unacceptable domains"):
      // Arrange
      val dbStub        = stub[EmailDbManager]
      val validatorStub = stub[ValidationService]
      validatorStub.validate.returns:
        case "test@badhost.com" => ValidationStatus.InvalidDomain
        case _                  => ValidationStatus.Valid
      val client = Client(dbStub, validatorStub)

      // Act - Test the client's validation logic
      val result = client.validateEmail("test@badhost.com").unsafeRunSync()

      // Assert
      result ==> ValidationStatus.InvalidDomain

    test("validateEmail returns ConnectionError when service is unreachable"):
      // Arrange
      val dbStub        = stub[EmailDbManager]
      val validatorStub = stub[ValidationService]
      validatorStub.validate.returns(_ => ValidationStatus.ConnectionError)
      val client = Client(dbStub, validatorStub)

      // Act - Test the client's validation logic
      val result = client.validateEmail("test@gmail.com").unsafeRunSync()

      // Assert
      result ==> ValidationStatus.ConnectionError

end ClientTests
