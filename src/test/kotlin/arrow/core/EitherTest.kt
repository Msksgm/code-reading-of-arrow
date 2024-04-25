package arrow.core

import io.kotest.assertions.fail
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import arrow.core.test.either
import io.kotest.property.arbitrary.long
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

    @Test
    fun rightIsLeftIsRight() = runTest {
        checkAll(Arb.int()) { a: Int ->
            val x = Either.Right(a)
            if (x.isRight()) x.value shouldBe a
            else fail("Right(a).isRight() cannot be false")
            x.isLeft() shouldBe false
        }
    }

    @Test
    fun tapAppliesEffects() = runTest {
        checkAll(Arb.either(Arb.long(), Arb.int())) { either ->
            var effect = 0
            val res = either.onRight { effect += 1}
            val expected = when (either) {
                is Either.Left -> 0
                is Either.Right -> 1
            }
            effect shouldBe expected
            res shouldBe either
        }
    }

    @Test
    fun tapLeftAppliesEffects() = runTest {
        checkAll(Arb.either(Arb.long(), Arb.int())) { either ->
            var effect = 0
            val res = either.onLeft { effect += 1}
            val expected = when (either) {
                is Either.Left -> 1
                is Either.Right -> 0
            }
            effect shouldBe expected
            res shouldBe either
        }
    }
}
