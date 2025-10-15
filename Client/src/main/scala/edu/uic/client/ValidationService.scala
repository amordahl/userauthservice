package edu.uic.client

import scala.util.{Failure, Success, Try}
import org.checkerframework.checker.units.qual.s

enum ValidationStatus:
  case Valid, InvalidConstruction, InvalidDomain, ConnectionError

class ValidationService():
  def validate(email: String): ValidationStatus =
    val validator =
      sys.env.getOrElse("VALIDATION_SERVICE", "localhost")
    try
      val r = requests.post(s"http://$validator:8080/validate", data = email)
      if ujson.read(r.text())("status").str == "OK" then ValidationStatus.Valid
      else
        if ujson.read(
            r.text()
          )("reason").str == "Unacceptable domain"
        then
          ValidationStatus.InvalidDomain
        else
          ValidationStatus.InvalidConstruction
      end if
    catch
      case e: Exception =>
        System.err.println(s"Validation request failed: ${e.getMessage}")
        ValidationStatus.ConnectionError
    end try
  end validate
end ValidationService
