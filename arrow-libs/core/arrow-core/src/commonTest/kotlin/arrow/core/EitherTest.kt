package arrow.core

import arrow.core.Either.Left
import arrow.core.Either.Right
import io.kotest.assertions.fail
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import arrow.core.test.either
import arrow.core.test.laws.MonoidLaws
import arrow.core.test.laws.intSmall
import arrow.core.test.nonEmptyList
import arrow.core.test.testLaws
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.byte
import io.kotest.property.arbitrary.char
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.float
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.short
import io.kotest.property.arbitrary.string
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class EitherTest {

  val ARB = Arb.either(Arb.string(), Arb.int())

  @Test
  fun monoidLaws() =
    testLaws(
      MonoidLaws("Either", 0.right(), { x, y -> x.combine(y, String::plus, Int::plus) }, ARB)
    )

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

  @Test
  fun foldOk() = runTest {
    checkAll(Arb.intSmall(), Arb.intSmall()) { a, b ->
      val right: Either<Int, Int> = Either.Right(a)
      val left: Either<Int, Int> = Either.Left(b)

      right.fold({ it + 2 }, { it + 1 }) shouldBe a + 1
      left.fold({ it + 2 }, { it + 1 }) shouldBe b + 2
    }
  }

  @Test
  fun combineTwoRights() = runTest {
    checkAll(Arb.string(), Arb.string()) { a: String, b: String ->
      Either.Right(a + b) shouldBe Either.Right(a).combine(
        Either.Right(b),
        Int::plus,
        String::plus
      )
    }
  }

  @Test
  fun combineTwoLefts() = runTest {
    checkAll(Arb.string(), Arb.string()) {a: String, b: String ->
      Either.Left(a + b) shouldBe Either.Left(a).combine(
        Either.Left(b),
        String::plus,
        Int::plus
      )
    }
  }

  @Test
  fun combineRightLeft() = runTest {
    checkAll(Arb.string(), Arb.string()) { a: String, b: String ->
      Either.Left(a) shouldBe Either.Left(a).combine(Either.Right(b), String::plus, String::plus)
      Either.Left(a) shouldBe Either.Right(a).combine(Either.Left(a), String::plus, String::plus)
    }
  }

  @Test
  fun getOrElseOk() = runTest {
    checkAll(Arb.int(), Arb.int()) { a: Int, b: Int ->
      Either.Right(a).getOrElse { b } shouldBe a
      Either.Left(a).getOrElse { b } shouldBe b
    }
  }

  @Test
  fun getOrNullOk() = runTest {
    checkAll(Arb.int()) { a: Int ->
      Either.Right(a).getOrNull() shouldBe a
    }
  }

  @Test
  fun getOrNoneRight() = runTest {
    checkAll(Arb.string()) { a: String ->
      Either.Right(a).getOrNone() shouldBe Some(a)
    }
  }

  @Test
  fun getOrNoneLeft() = runTest {
    checkAll(Arb.string()) { a: String ->
      Either.Left(a).getOrNone() shouldBe None
    }
  }

  @Test
  fun swapOk() = runTest {
    checkAll(Arb.int()) { a: Int ->
      Either.Right(a).swap() shouldBe Either.Left(a)
      Either.Left(a).swap() shouldBe Either.Right(a)
    }
  }

  @Test
  fun mapOnlyRight() = runTest {
    checkAll(Arb.intSmall(), Arb.intSmall()) { a, b ->
      val right: Either<Int, Int> = Either.Right(a)
      val left: Either<Int, Int> = Either.Left(b)

      right.map { it + 1 } shouldBe  Either.Right(a + 1)
      left.map { it + 1 } shouldBe left
    }
  }

  @Test
  fun mapLeftOnlyLeft() = runTest {
    checkAll(Arb.intSmall(), Arb.intSmall()) { a, b ->
      val right: Either<Int, Int> = Either.Right(a)
      val left: Either<Int, Int> = Either.Left(b)

      right.mapLeft { it + 1 } shouldBe right
      left.mapLeft { it + 1 } shouldBe Either.Left(b + 1)
    }
  }

  @Test
  fun flatMapOnlyRight() = runTest {
    checkAll(Arb.intSmall(), Arb.intSmall()) { a, b ->
      val right: Either<Int, Int> = Either.Right(a)
      val left: Either<Int, Int> = Either.Left(b)

      right.flatMap { Either.Right(it + 1) } shouldBe Either.Right(a + 1)
      left.flatMap { Either.Right(it + 1) } shouldBe left
    }
  }

  @Test
  fun zipOrAccumulateList() = runTest {
    checkAll(
      Arb.either(Arb.string(), Arb.short()),
      Arb.either(Arb.string(), Arb.byte()),
      Arb.either(Arb.string(), Arb.int()),
      Arb.either(Arb.string(), Arb.long()),
      Arb.either(Arb.string(), Arb.float()),
      Arb.either(Arb.string(), Arb.double()),
      Arb.either(Arb.string(), Arb.char()),
      Arb.either(Arb.string(), Arb.string()),
      Arb.either(Arb.string(), Arb.boolean())
    ) { a, b, c, d, e, f, g, h, i ->
      val res = Either.zipOrAccumulate({ e1, e2 -> "$e1$e2" }, a, b, c, d, e, f, g, h, i, ::Tuple9)
      val all = listOf(a, b, c, d, e, f, g, h, i)

      val expected = if (all.any { it.isLeft() }) {
        all.filterIsInstance<Left<String>>().fold("") { acc, t -> "$acc${t.value}" }.left()
      } else {
        all.filterIsInstance<Right<Any?>>().map { it.value }.let {
          Tuple9(it[0], it[1], it[2], it[3], it[4], it[5], it[6], it[7], it[8]).right()
        }
      }

      res shouldBe expected
    }
  }

  @Test
  fun zipOrAccumulateSemigroup() = runTest {
    checkAll(
      Arb.either(Arb.string(), Arb.short()),
      Arb.either(Arb.string(), Arb.byte()),
      Arb.either(Arb.string(), Arb.int()),
      Arb.either(Arb.string(), Arb.long()),
      Arb.either(Arb.string(), Arb.float()),
      Arb.either(Arb.string(), Arb.double()),
      Arb.either(Arb.string(), Arb.char()),
      Arb.either(Arb.string(), Arb.string()),
      Arb.either(Arb.string(), Arb.boolean())
    ) { a, b, c, d, e, f, g, h, i ->
      val res = Either.zipOrAccumulate(a, b, c, d, e, f, g, h, i, ::Tuple9)
      val all = listOf(a, b, c, d, e, f, g, h, i)

      val expected = if (all.any { it.isLeft() }) {
        all.filterIsInstance<Left<String>>().map { it.value }.toNonEmptyListOrNull()!!.left()
      } else {
        all.filterIsInstance<Right<Any?>>().map { it.value }.let {
          Tuple9(it[0], it[1], it[2], it[3], it[4], it[5], it[6], it[7], it[8]).right()
        }
      }

      res shouldBe expected
    }
  }

  @Test
  fun zipOrAccumulateEitherNel() = runTest {
    checkAll(
      Arb.either(Arb.nonEmptyList(Arb.int()), Arb.short()),
      Arb.either(Arb.nonEmptyList(Arb.int()), Arb.byte()),
      Arb.either(Arb.nonEmptyList(Arb.int()), Arb.int()),
      Arb.either(Arb.nonEmptyList(Arb.int()), Arb.long()),
      Arb.either(Arb.nonEmptyList(Arb.int()), Arb.float()),
      Arb.either(Arb.nonEmptyList(Arb.int()), Arb.double()),
      Arb.either(Arb.nonEmptyList(Arb.int()), Arb.char()),
      Arb.either(Arb.nonEmptyList(Arb.int()), Arb.string()),
      Arb.either(Arb.nonEmptyList(Arb.int()), Arb.boolean())
    ) { a, b, c, d, e, f, g, h, i ->
      val res = Either.zipOrAccumulate(a, b, c, d, e, f, g, h, i, ::Tuple9)
      val all = listOf(a, b, c, d, e, f, g, h, i)

      val expected = if (all.any { it.isLeft() }) {
        all.filterIsInstance<Left<NonEmptyList<String>>>()
          .flatMap { it.value }
          .toNonEmptyListOrNull()!!.left()
      } else {
        all.filterIsInstance<Right<Any?>>().map { it.value }.let {
          Tuple9(it[0], it[1], it[2], it[3], it[4], it[5], it[6], it[7], it[8]).right()
        }
      }

      res shouldBe expected
    }
  }
}
