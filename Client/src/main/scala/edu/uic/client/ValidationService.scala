package edu.uic.client

import scala.concurrent.Future

final class ValidationService():

  def validate(email: String): Boolean =
    val validator = sys.env.getOrElse("VALIDATOR", "http://localhost:8080")
    println(s"Using validator service at $validator")
    val response = requests.post(s"$validator/validate", data = email)
    response.statusCode == 200
  end validate
end ValidationService
