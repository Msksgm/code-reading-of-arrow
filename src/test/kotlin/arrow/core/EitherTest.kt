package arrow.core

import io.kotest.assertions.fail
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll
import arrow.core.test.either
import arrow.core.test.laws.intSmall
import arrow.typeclasses.Monoid
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.nonPositiveInt
import io.kotest.property.arbitrary.string

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

    "bifoldLeft should apply first op if Left and apply second op if Right" {
        checkAll(Arb.intSmall(), Arb.intSmall(), Arb.intSmall()) { a, b, c ->
            Either.Right(a).bifoldLeft(c, Int::plus, Int::times) shouldBe a * c
            Either.Left(b).bifoldLeft(c, Int::plus, Int::times) shouldBe b + c
        }
    }

    "bifoldMap should apply first op if Left and apply second op if Right" {
        checkAll(Arb.intSmall(), Arb.intSmall()) { a, b ->
            val right: Either<Int, Int> = Either.Right(a)
            val left: Either<Int, Int> = Either.Left(b)

            right.bifoldMap( Monoid.int(), { it + 2}, { it + 1 }) shouldBe a + 1
            left.bifoldMap( Monoid.int(), { it + 2}, { it + 1 }) shouldBe b + 2
        }
    }

    "fromNullable should lift value as a Right if it is not null" {
        checkAll(Arb.int()) { a ->
            Either.fromNullable(a) shouldBe Either.Right(a)
        }
    }

    "fromNullable should lift value as a Left(Unit) if it is null" {
        Either.fromNullable(null) shouldBe Either.Left(Unit)
    }

    "empty should return a Right of the empty of the inner type" {
        Either.Right(Monoid.string().empty()) shouldBe Monoid.either(Monoid.string(), Monoid.string()).empty()
    }

    "combine two rights should return a right of the combine of the inners" {
        checkAll(Arb.string(), Arb.string()) { a: String, b: String ->
            Monoid.string().run { Either.Right(a.combine(b))} shouldBe Either.Right(a).combine(
                Monoid.string(),
                Monoid.string(),
                Either.Right(b)
            )
        }
    }

    "combine two lefts should return a left of the combine of the inners" {
        checkAll(Arb.string(), Arb.string()) { a: String, b: String ->
            Monoid.string().run { Either.Left(a.combine(b)) } shouldBe Either.Left(a).combine(Monoid.string(), Monoid.string(), Either.Left(b))
        }
    }

    "combine a right and a left should return left" {
        checkAll(Arb.string(), Arb.string()) { a: String , b: String ->
            Either.Left(a) shouldBe Either.Left(a).combine(Monoid.string(), Monoid.string(), Either.Right(b))
            Either.Left(a) shouldBe Either.Right(a).combine(Monoid.string(), Monoid.string(), Either.Left(a))
        }
    }

    "getOrElse should return value" {
        checkAll(Arb.int(), Arb.int()) { a: Int, b: Int ->
            Either.Right(a).getOrElse { b } shouldBe a
            Either.Left(a).getOrElse { b } shouldBe b
        }
    }

    "orNull should return value" {
        checkAll(Arb.int()) { a: Int ->
            Either.Right(a).orNull() shouldBe a
        }
    }

    "orNone should return Some(value)" {
        checkAll(Arb.int()) { a: Int ->
            Either.Right(a).orNone() shouldBe Some(a)
        }
    }

    "orNone should return None when left" {
        checkAll(Arb.string()) { a: String ->
            Either.Left(a).orNone() shouldBe None
        }
    }

    "getOrHandle should value" {
        checkAll(Arb.int(), Arb.int()) { a: Int, b: Int ->
            Either.Right(a).getOrHandle { b } shouldBe a
            Either.Left(a).getOrHandle { it + b } shouldBe a + b
        }
    }

    "filterOrElse should filter values" {
        checkAll(Arb.intSmall(), Arb.intSmall()) { a, b ->
            val left: Either<Int, Int> = Either.Left(a)

            Either.Right(a).filterOrElse( { it > a - 1 }, { b }) shouldBe Either.Right(a)
            Either.Right(a).filterOrElse( { it > a + 1 }, { b }) shouldBe Either.Left(b)
            left.filterOrElse( { it > a -1 }, { b }) shouldBe Either.Left(a)
            left.filterOrElse( { it > a +1 }, { b }) shouldBe Either.Left(a)
        }
    }

    "filterOrOther should filter values" {
        checkAll(Arb.intSmall(), Arb.intSmall()) { a, b ->
            val left: Either<Int, Int> = Either.Left(a)

            Either.Right(a).filterOrOther( { it > a - 1 }, { b }) shouldBe Either.Right(a)
            Either.Right(a).filterOrOther( { it > a + 1 }, { b }) shouldBe Either.Left(b)
            left.filterOrOther( { it > a -1 }, { b }) shouldBe Either.Left(a)
            left.filterOrOther( { it > a +1 }, { b }) shouldBe Either.Left(a)
        }
    }

    "leftIfNull should return Left if Right value is null of if Either is Left" {
        checkAll(Arb.int()) { a: Int ->
            Either.Right(a).leftIfNull { a } shouldBe Either.Right(a)
            Either.Right(null).leftIfNull { a } shouldBe Either.Left(a)
            Either.Left(a).leftIfNull { a } shouldBe Either.Left(a)
        }
    }

    "exists should apply predicate to Right only" {
        checkAll(Arb.intSmall()) { a ->
            val left: Either<Int, Int> = Either.Left(a)

            Either.Right(a).exists { it > a - 1 } shouldBe true
            !Either.Right(a).exists { it > a + 1 } shouldBe true
            !left.exists { it > a - 1 } shouldBe true
            !left.exists { it > a + 1 } shouldBe true
        }
    }

    "rightIfNotNull should return Left if value is null or Right of value when not null" {
        checkAll(Arb.int(), Arb.int()) { a: Int, b: Int ->
            null.rightIfNotNull { b } shouldBe Either.Left(b)
            a.rightIfNotNull { b } shouldBe Either.Right(a)
        }
    }


    "rightIfNull should return Left if value is not null or Right of value when null" {
        checkAll(Arb.int(), Arb.int()) { a: Int, b: Int ->
            a.rightIfNull { b } shouldBe Either.Left(b)
            null.rightIfNull { b } shouldBe Either.Right(null)
        }
    }

    "swap should interchange values" {
        checkAll(Arb.int()) { a: Int ->
            Either.Left(a).swap() shouldBe Either.Right(a)
            Either.Right(a).swap() shouldBe Either.Left(a)
        }
    }

    "orNull should convert" {
        checkAll(Arb.int()) { a: Int ->
            val left: Either<Int, Int> = Either.Left(a)

            Either.Right(a).orNull() shouldBe a
            left.orNull() shouldBe null
        }
    }

    "contains should check value" {
        checkAll(Arb.intSmall(), Arb.intSmall()) { a, b ->
            require(Either.Right(a).contains(a)) { "Expected ${Either.Right(a)}.contains($a) to be true, but it was false."}
            if (a != b) require(!Either.Right(a).contains(b)) { "Expected ${Either.Right(a)}.contains($b) to be false, but it was true."}
            else require(Either.Right(a).contains(b)) { "Expected ${Either.Right(a)}.contains($b) to be true, but it was false."}
            require(!Either.Left(a).contains(a)) { "Expected ${Either.Left(a)}.contains($a) to be false, but it was true."}
        }
    }

    "map should alter right instance only" {
        checkAll(Arb.intSmall(), Arb.intSmall()) { a, b ->
            val right: Either<Int, Int> = Either.Right(a)
            val left: Either<Int, Int> = Either.Left(b)

            right.map { it + 1 } shouldBe Either.Right(a + 1)
            left.map { it + 1 } shouldBe left
        }
    }

    "mapLeft should alter left instance only" {
        checkAll(Arb.intSmall(), Arb.intSmall()) { a, b ->
            val right: Either<Int, Int> = Either.Right(a)
            val left: Either<Int, Int> = Either.Left(b)

            right.mapLeft { it + 1 } shouldBe right
            left.mapLeft { it + 1 } shouldBe Either.Left(b + 1)
        }
    }

    "bimap should alter left or right instance accordingly" {
        checkAll(Arb.intSmall(), Arb.intSmall()) { a, b ->
            val right: Either<Int, Int> = Either.Right(a)
            val left: Either<Int, Int> = Either.Left(b)

            right.bimap({ it + 2 }, { it + 1}) shouldBe Either.Right(a + 1)
            left.bimap({ it + 2 }, { it + 1}) shouldBe Either.Left(b + 2)
        }
    }

    "replicate should return Right(empty list) when n <=0" {
        checkAll(
            Arb.nonPositiveInt(),
            Arb.int(0..100)
        ) { n: Int, a: Int ->
            val expected: Either<Int, List<Int>> = Either.Right(emptyList())

            Either.Right(a).replicate(n) shouldBe expected

            Either.Left(a).replicate(n) shouldBe expected
        }
    }

    "replicate should return Right(list of repeated value size n) when Right and n is positive" {
        checkAll(
            Arb.int(1..10),
            Arb.int()
        ) { n: Int, a: Int ->
            Either.Right(a).replicate(n) shouldBe Either.Right(List(n) { a })
            Either.Left(a).replicate(n) shouldBe Either.Left(a)
        }
    }

    "traverse should return list of Right when Right and singleton list when Left" {
        checkAll(
            Arb.int(),
            Arb.int(),
            Arb.int()
        ) { a: Int, b: Int, c: Int ->
            Either.Right(a).traverse { emptyList<Int>() } shouldBe emptyList<Int>()
            Either.Right(a).traverse { listOf(b, c) } shouldBe listOf(Either.Right(b), Either.Right(c))
            Either.Left(a).traverse { listOf(b, c) } shouldBe listOf(Either.Left(a))
        }
    }

    "flatMap should map right instance only" {
        checkAll(Arb.intSmall(), Arb.intSmall()) { a, b ->
            val right: Either<Int, Int> = Either.Right(a)
            val left: Either<Int, Int> = Either.Left(b)

            right.flatMap { Either.Right(it + 1) } shouldBe Either.Right(a + 1)
            left.flatMap { Either.Right(it + 1) } shouldBe left
        }
    }

    "conditionally should create right instance only if test is true" {
        checkAll(Arb.boolean(), Arb.int(), Arb.string()) { t: Boolean, i: Int , s: String ->
            val expected = if (t) Either.Right(i) else Either.Left(s)
            Either.conditionally(t, { s }, { i }) shouldBe expected
        }
    }

    "combine should combine 2 eithers" {
        checkAll(Arb.either(Arb.string(), Arb.int()), Arb.either(Arb.string(), Arb.int())) { e1, e2 ->
            val obtained = e1.combine(e2, { l1, l2 -> l1 + l2 }, { r1, r2 -> r1 + r2 })
            val expected = when(e1){
                is Either.Left -> when(e2) {
                    is Either.Left -> Either.Left(e1.value + e2.value)
                    is Either.Right -> e1
                }
                is Either.Right -> when(e2) {
                    is Either.Left -> e2
                    is Either.Right -> Either.Right(e1.value + e2.value)
                }
            }
            obtained shouldBe expected
        }
    }

    "combineAll replacement should work" {
        checkAll(Arb.list(Arb.either(Arb.string(), Arb.int()))) { list ->
            val obtained = list.fold<Either<String, Int>, Either<String, Int>>(0.right()) { x, y ->
                Either.zipOrAccumulate<String, Int, Int, Int>(
                    { a1, a2 -> a1 + a2},
                    x,
                    y,
                    { b1, b2 -> b1 + b2}
                )
            }
            val expected = list.combineAll(Monoid.string(), Monoid.int())
            obtained shouldBe expected
        }
    }

    "traverse should return list if either is right" {
        val right: Either<String, Int> = Either.Right(1)
        val left: Either<String, Int> = Either.Left("foo")

        right.traverse { listOf(it, 2, 3) } shouldBe listOf(Either.Right(1), Either.Right(2), Either.Right(3))
        left.traverse { listOf(it, 2, 3) } shouldBe listOf(Either.Left("foo"))
    }

    "sequence should be consistent with traverse" {
        checkAll(Arb.either(Arb.string(), Arb.int())) { either ->
            either.map { listOf(it) }.sequence() shouldBe either.traverse { listOf(it) }
        }
    }
})
