package arrow.core

import io.kotest.assertions.fail
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import arrow.core.test.either
import io.kotest.property.arbitrary.string
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class EitherTest {
    val ARB = Arb.either(Arb.string(), Arb.int())

//    @Test
//    fun monoidLaws() =
//        testLaws(
//            MonoidLaws("Either", 0.right(), { x, y -> x.combine(y, String::plus, Int::plus) }, ARB)
//        )

    @Test
    fun leftIsLeftIsRight() = runTest {
        checkAll(Arb.int()) { a: Int ->
            val x = Either.Left(a)
            if (x.isLeft()) x.value shouldBe a
            else fail("Left(a).isLeft() cannot be false")
            x.isRight() shouldBe false
        }
    }
}
