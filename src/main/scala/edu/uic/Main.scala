package edu.uic

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

enum UserStatus:
  case Normal, Blocked

enum FailedAuthResult:
  case UserNotFound, UserNotAllowed, WrongPassword

case class User(id: Long, status: UserStatus)

trait UserService:
  def findUser(userId: Long): Future[Option[User]]

trait PasswordService:
  def checkPassword(id: Long, password: String): Future[Boolean]

class UserAuthService(
    userService: UserService,
    passwordService: PasswordService
):
  def authorize(
      id: Long,
      password: String
  ): Future[Either[FailedAuthResult, User]] =
    userService.findUser(id).flatMap:
      case None =>
        Future.successful(Left(FailedAuthResult.UserNotFound)) 
      case Some(user) if user.status == UserStatus.Blocked =>
        Future.successful(Left(FailedAuthResult.UserNotAllowed))
      case Some(user) =>
        passwordService.checkPassword(id, password).map:
          case true  => Right(user)
          case false => Left(FailedAuthResult.WrongPassword)

end UserAuthService
