package ru.org.codingteam.loprog

object LoprogRepl {
  def start(fileName: String) = {
    val predicates =
      LoprogParsers.parse(
        LoprogParsers.sourceCode,
        scala.io.Source.fromFile(fileName).mkString
      )

    if(predicates.successful) {
      while(true) {
        print("?- ")

        val query = LoprogParsers.parse(LoprogParsers.query, readLine)

        if(query.successful) {
          val vars = Utils.collectVars(query.get)

          Loprog.visitSolutions(predicates.get, query.get, {
            bindings => {
              for(varName <- vars)
                if(bindings.contains(varName))
                  println(varName + " = " + Loprog.showValue(varName, bindings))

              readLine
            }
          })
        } else {
          println(query)
        }
      }
    } else {
      println(predicates)
    }
  }
}
