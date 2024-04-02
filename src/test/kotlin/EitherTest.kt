import io.kotest.assertions.fail
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

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
})
