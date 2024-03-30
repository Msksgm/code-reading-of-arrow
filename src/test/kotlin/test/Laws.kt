package test

import io.kotest.core.names.TestName
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.spec.style.scopes.StringSpecScope
import io.kotest.core.spec.style.scopes.addTest
import io.kotest.core.test.TestContext

interface LawSet {
  val laws: List<Law>
}

data class Law(val name: String, val test: suspend TestContext.() -> Unit)

fun StringSpec.testLaws(lawSet: LawSet): Unit = testLaws(lawSet.laws)

fun StringSpec.testLaws(vararg laws: List<Law>): Unit = laws
  .flatMap { list: List<Law> -> list.asIterable() }
  .distinctBy { law: Law -> law.name }
  .forEach { law: Law ->
    addTest(TestName(null, law.name, false), false, null) {
      law.test(StringSpecScope(this.coroutineContext, testCase))
    }
  }
