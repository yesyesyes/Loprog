package ru.org.codingteam.loprog

import scala.io.Source
import scala.collection.mutable.ListBuffer
import jline.console.ConsoleReader
import jline.TerminalFactory

object LoprogRepl {
  def readQuery(con: ConsoleReader): String = {
    var result = new ListBuffer[String]
    var piece = con.readLine().trim

    while(piece.length() != 0 && piece.last != '.') {
      result.append(piece)
      con.setPrompt("|  ")
      piece = con.readLine().trim
    }

    result.append(piece)
    result.mkString("\n")
  }

  def launch(fileName: String) {
    try {
      start(fileName)
    } finally { TerminalFactory.get().restore(); }
  }

  private def start(fileName: String) = {
    val con = new ConsoleReader()
    val predicatesParseResult =
      LoprogParsers.parse(
        LoprogParsers.sourceCode,
        LoprogParsers.removeComments(Source.fromFile(fileName).mkString)
      )

    if(predicatesParseResult.successful) {
      val predicates = predicatesParseResult.get

      while(true) {
        con.setPrompt("?- ")

        val queryParseResult = LoprogParsers.parse(
          LoprogParsers.query,
          LoprogParsers.removeComments(readQuery(con))
        )

        if(queryParseResult.successful) {
          val query = queryParseResult.get
          val vars = Utils.collectVars(query)

          Loprog.visitSolutions(predicates, query, {
            bindings => {
              val answer = vars.filter(bindings.contains(_)).map({
                varName => s"$varName = ${Loprog.showValue(varName, bindings)}"
              }).mkString(",\n")

              con.print(answer + " "); con.flush()
              if(con.readCharacter() == ';') {
                con.println(";");
                Next
              }
              else {
                con.println("")
                Abort
              }
            }
          })
        } else {
          con.println(queryParseResult toString())
        }
      }
    } else {
      con.println(predicatesParseResult toString())
    }
  }
}
