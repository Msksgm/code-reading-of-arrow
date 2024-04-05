import io.kotest.assertions.fail
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll
import test.either
import test.laws.intSmall
import typeclasses.Monoid

class EitherTest : StringSpec({
    "isLeft shoud return true if Left and false if Right" {
        checkAll(Arb.int()) {a : Int ->
            val x = Either.Left(a)
            if (x.isLeft()) x.value shouldBe a
            else fail("Left(a).isRight() should be false")
            x.isRight() shouldBe false
        }
    }

    "isRight should return false if Left and true if Right" {
        checkAll(Arb.int()) {a : Int ->
            val x = Either.Right(a)
            if (x.isRight()) x.value shouldBe a
            else fail("Right(a).isRight() should be true")
            x.isLeft() shouldBe false
        }
    }

    "tap applies effects returning the original value" {
        checkAll(Arb.either(Arb.long(), Arb.int())) { either ->
            var effect = 0
            val res = either.tap { effect += 1}
            val expected = when (either) {
                is Either.Left -> 0
                is Either.Right -> 1
            }
            effect shouldBe expected
            res shouldBe either
        }
    }

    "tapLeft applies effects returning the original value" {
        checkAll(Arb.either(Arb.long(), Arb.int())) { either ->
            var effect = 0
            val res = either.tapLeft{effect += 1}
            val expected = when (either) {
                is Either.Left -> 1
                is Either.Right -> 0
            }
            effect shouldBe expected
            res shouldBe either
        }
    }

    "fold should apply first op if Left and second op if Right" {
        checkAll(Arb.intSmall(), Arb.intSmall()) {a, b ->
            val right: Either<Int, Int> = Either.Right(a)
            val left: Either<Int, Int> = Either.Left(b)

            right.fold({it + 2}, {it + 1}) shouldBe a + 1
            left.fold({it + 2}, {it + 1}) shouldBe b + 2
        }
    }

    "foldLeft should return initial if Left and apply op if Right" {
        checkAll<Int, Int, Int>(Arb.intSmall(), Arb.intSmall(), Arb.intSmall()) { a, b, c ->
            Either.Right(a).foldLeft(c, Int::plus) shouldBe c + a
            Either.Left(b).foldLeft(c, Int::plus) shouldBe c
        }
    }

    "foldMap should return the empty of the inner type if Left and apply op if Right" {
        checkAll(Arb.intSmall(), Arb.intSmall()) { a, b ->
            val left: Either<Int, Int> = Either.Left(b)

            Either.Right(a).foldMap(Monoid.int()) { it + 1 } shouldBe a + 1
            left.foldMap(Monoid.int()) { it + 1 } shouldBe Monoid.int().empty()
        }
    }
})
