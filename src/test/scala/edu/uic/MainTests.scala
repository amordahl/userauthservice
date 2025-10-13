package edu.uic

import munit.FunSuite
import org.scalamock.stubs.{Stub, Stubs}
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

// For more information on writing tests, see
// https://scalameta.org/munit/docs/getting-started.html
class MySuite extends FunSuite, Stubs:

  /** Fixture for each test case */
  val env =
    FunFixture[(Stub[UserService], Stub[PasswordService], UserAuthService)](
      setup = _ =>
        val userService     = stub[UserService]
        val passwordService = stub[PasswordService]
        userService.findUser.returns:
          case `userId` => Future.successful(Some(user))
          case _        => Future.successful(None)
        passwordService.checkPassword.returns:
          case (`userId`, `password`) => Future.successful(true)
          case _                      => Future.successful(false)
        val authService = UserAuthService(userService, passwordService)
        (userService, passwordService, authService)
      ,
      teardown = _ => ()
    )

  val userId      = 100
  val user        = User(userId, UserStatus.Normal)
  val blockedUser = User(userId, UserStatus.Blocked)
  val password    = "password"

  env.test("Happy path"): (_, _, auth) =>
    // Set up how the services should behave
    val result = auth.authorize(userId, password)
    assertEquals(Await.result(result, 2.seconds), Right(user))

  env.test("Return user for known id"): (_, _, auth) =>
    val result = auth.authorize(userId + 10, password)
    assertEquals(
      Await.result(result, 2.seconds),
      Left(FailedAuthResult.UserNotFound)
    )

  env.test("User blocked"): (uS, _, aS) =>
    uS.findUser.returns:
      case `userId` => Future.successful(Some(blockedUser))
      case _        => Future.successful(None)

    val result = aS.authorize(userId, password)
    assertEquals(
      Await.result(result, 2.seconds),
      Left(FailedAuthResult.UserNotAllowed)
    )

end MySuite
