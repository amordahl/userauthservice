import cask.*
import scala.util.matching.Regex
import ujson.*
import upickle.default.write
import scala.language.adhocExtensions

object AuthService extends cask.MainRoutes:
  @cask.get("/")
  def rootReply() =
    """
    |Email Validation API
    |========================
    |POST /validate - Validate an email address
    |POST /validate/json - Validates an email address in JSON format ({"email": "xxx"})
    |GET  /health   - Health check
    """.stripMargin
  end rootReply

  type Local  = String
  type Domain = String

  enum ValidationFailures:
    case CouldNotParse, UnacceptableDomain

  private def validateEmail(email: String)
      : Either[ValidationFailures, (Local, Domain)] =
    val emailRegex =
      raw"^([a-zA-Z0-9._%+-]+)@([a-zA-Z0-9.-]+\.([a-zA-Z]{2,}))$$"
    val stringMatch = Regex(emailRegex)
    email match
      case stringMatch(local, domain, _) =>
        if Set(
            "gmail.com",
            "yahoo.com",
            "outlook.com",
            "uic.edu"
          ).contains(domain)
        then
          Right((local, domain))
        else
          Left(ValidationFailures.UnacceptableDomain)
      case _ =>
        Left(ValidationFailures.CouldNotParse)
    end match
  end validateEmail

  def validateHelper(email: String) =
    validateEmail(email) match
      case Right((local, domain)) =>
        cask.Response(
          write(Map(
            "status" -> "OK",
            "local"  -> local,
            "domain" -> domain
          )),
          statusCode = 200,
          headers = Seq(("Content-Type", "application/json"))
        )
      case Left(validationError) =>
        val response = validationError match
          case ValidationFailures.CouldNotParse =>
            Map("status" -> "FAILED", "reason" -> "Could not parse")
          case ValidationFailures.UnacceptableDomain =>
            Map("status" -> "FAILED", "reason" -> "Unacceptable domain")
        cask.Response(
          write(response),
          statusCode = 200,
          headers = Seq(("Content-Type", "application/json"))
        )

  @cask.post("/validate")
  def validateEndpoint(email: cask.Request) =
    validateHelper(email.text())

  @cask.postJson("/validate/json")
  def validateJsonEndpoint(email: String) =
    validateHelper(email)

  @cask.get("/health")
  def healthEndpoint() =
    cask.Response(
      write(Map("status" -> "OK")),
      statusCode = 200,
      headers = Seq(("Content-Type", "application/json"))
    )

  override def host: String = "0.0.0.0"
  println("Starting auth service...")
  initialize()
end AuthService
