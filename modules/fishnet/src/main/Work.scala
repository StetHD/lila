package lila.fishnet

import org.joda.time.DateTime

import chess.format.FEN
import chess.variant.Variant

sealed trait Work {
  def _id: Work.Id
  def game: Work.Game
  def tries: Int
  def acquired: Option[Work.Acquired]
  def createdAt: DateTime

  def acquiredByKey = acquired.map(_.clientKey)

  def isAcquiredBy(client: Client) = acquiredByKey contains client.key
}

object Work {

  case class Id(value: String) extends AnyVal

  case class Acquired(
    clientKey: Client.Key,
    date: DateTime)

  case class Game(
    id: String,
    initialFen: Option[FEN],
    variant: Variant,
    moves: Seq[String])

  case class Move(
      _id: Work.Id, // random
      game: Game,
      currentFen: FEN,
      level: Int,
      tries: Int,
      acquired: Option[Acquired],
      createdAt: DateTime) extends Work {

    def id = _id

    def assignTo(client: Client) = copy(
      acquired = Acquired(clientKey = client.key, date = DateTime.now).some,
      tries = tries + 1)

    def timeout = copy(acquired = none)

    def invalid = copy(acquired = none)

    def isOutOfTries = tries >= 3

    override def toString = s"id:$id game:${game.id} level:$level tries:$tries currentFen:$currentFen"
  }

  case class Analysis(
      _id: Work.Id, // random
      game: Game,
      tries: Int,
      acquired: Option[Acquired],
      createdAt: DateTime) extends Work {

    def id = _id

    def assignTo(client: Client) = copy(
      acquired = Acquired(clientKey = client.key, date = DateTime.now).some,
      tries = tries + 1)
  }

  def makeId = Id(scala.util.Random.alphanumeric take 10 mkString)
}