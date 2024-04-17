package arrow.core.test

import arrow.core.Either
import arrow.core.NonEmptyList
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map

fun <A> Arb.Companion.nonEmptyList(arb: Arb<A>, range: IntRange = 0..100): Arb<NonEmptyList<A>> =
    Arb.bind(arb, Arb.list(arb, range), ::NonEmptyList)

fun <E, A> Arb.Companion.either(arbE: Arb<E>, arbA: Arb<A>): Arb<Either<E, A>> {
    val arbLeft = arbE.map { Either.Left(it) }
    val arbRight = arbA.map { Either.Right(it) }
    return Arb.choice(arbLeft, arbRight)
}
