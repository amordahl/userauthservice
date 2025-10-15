package edu.uic.client

import scala.concurrent.Future
import slick.jdbc.PostgresProfile.api.*
import slick.lifted.ProvenShape

final class EmailDbManager(db: Database):
  type Email = String

  case class EmailRecord(id: Option[Int], email: Email)
  case class Emails(tag: Tag) extends Table[EmailRecord](tag, "emails"):
    def id    = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def email = column[String]("email", O.Unique)
    def *     = (id.?, email).mapTo[EmailRecord]

  val emailTable  = TableQuery[Emails]
  val createQuery = emailTable.schema.createIfNotExists
  db.run(createQuery.transactionally): Unit

  def getAllEmails: Future[Seq[EmailRecord]] =
    val query = emailTable.result
    db.run(query)
  end getAllEmails

  def insertEmail(email: Email): Future[Int] =
    val insertQuery =
      emailTable returning emailTable.map(_.id) += EmailRecord(None, email)
    db.run(insertQuery)

  def getEmailById(id: Int): Future[Option[Email]] =
    val query = emailTable.filter(_.id === id).map(_.email).result.headOption
    db.run(query)
end EmailDbManager
