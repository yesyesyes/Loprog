import org.scalatest.FunSuite
import scala.collection.mutable.ListBuffer
import ru.org.codingteam.loprog._

class LoprogSuite extends FunSuite {
  test("unify (simple unification)") {
    // p(X, f(Y), a)
    val left = Functor("p",
      List(
        Variable("X"),
        Functor("f", List(Variable("Y"))),
        Functor("a", List())
      )
    )

    // p(a, f(a), Y)
    val right = Functor("p",
      List(
        Functor("a", List()),
        Functor("f", List(Functor("a", List()))),
        Variable("Y")
      )
    )

    // X = a; Y = a.
    val answer = Map(
      "X" -> Functor("a", List()),
      "Y" -> Functor("a", List())
    )

    // ?- p(X, f(Y), a) = p(a, f(a), Y).
    val bindings = Loprog.unify(left, right, Map())

    assert(bindings === Some(answer))
  }

  test("unify (unification with shared references)") {
    // p(X, f(Y), a)
    val left = Functor("p",
      List(
        Variable("X"),
        Functor("f", List(Variable("Y"))),
        Functor("a", List())
      )
    )

    // p(Z, f(b), a)
    val right = Functor("p",
      List(
        Variable("Z"),
        Functor("f", List(Functor("b", List()))),
        Functor("a", List())
      )
    )

    // ?- p(X, f(Y), a) = p(Z, f(b), a).
    // X = Z; Y = b.
    Loprog.unify(left, right, Map()) match {
      case Some(bindings) => {
        assert(bindings === Map("X" -> Variable("Z"), "Y" -> Functor("b", List())))
        // FIXME(rexim): omg, make this shorter.
        assert(Loprog.unify(Variable("X"), Functor("d", List()), bindings) === Some(Map("X" -> Variable("Z"), "Y" -> Functor("b", List()), "Z" -> Functor("d", List()))))
      }

      case None =>
        assert(false, s"$left doesn't unify $right")
    }
  }

  test("visitSolutions") {
    val program = List(
      // p(a).
      Predicate(Functor("p", List(Functor("a", List()))), List()),

      // p(X) :- q(X), r(X).
      Predicate(Functor("p", List(Variable("X"))),
        List(
          Functor("q", List(Variable("X"))),
          Functor("r", List(Variable("X")))
        )),

      // p(X) :- u(X).
      Predicate(Functor("p", List(Variable("X"))),
        List(
          Functor("u", List(Variable("X")))
        )),

      // q(X) :- s(X).
      Predicate(Functor("q", List(Variable("X"))),
        List(
          Functor("s", List(Variable("X")))
        )),

      // r(a).
      Predicate(Functor("r", List(Functor("a", List()))), List()),

      // r(b).
      Predicate(Functor("r", List(Functor("b", List()))), List()),

      // s(a).
      Predicate(Functor("s", List(Functor("a", List()))), List()),

      // s(b).
      Predicate(Functor("s", List(Functor("b", List()))), List()),

      // s(c).
      Predicate(Functor("s", List(Functor("c", List()))), List()),

      // u(d).
      Predicate(Functor("u", List(Functor("d", List()))), List())
    )

    // ?- p(X).
    val query = List(Functor("p", List(Variable("X"))))

    val solutions = new ListBuffer[Map[String, Term]]()

    Loprog.visitSolutions(program, query,
      (bindings: Map[String, Term]) => {
        solutions.append(bindings)
      }, Map())

    val answer = List(
      Some(Functor("a", List())),
      Some(Functor("a", List())),
      Some(Functor("b", List())),
      Some(Functor("d", List()))
    )

    assert(solutions.toList.map(_.get("X")) === answer)
  }

  test("addPrefixToVars") {
    // foo(X, a, bar(Y))
    val term = Functor("foo",
      List(
        Variable("X"),
        Functor("a", List()),
        Functor("bar", List(Variable("Y")))
      )
    )

    // foo(prefix::X, a, bar(prefix::Y))
    val answer = Functor("foo",
      List(
        Variable("prefix::X"),
        Functor("a", List()),
        Functor("bar", List(Variable("prefix::Y")))
      )
    )

    val result = Loprog.addPrefixToVars("prefix", term)

    assert(result === answer)
  }

  test("scopePredicate") {
    assert(false, "Test is not written yet.");
  }
}
