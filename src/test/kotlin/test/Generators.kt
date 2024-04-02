package test

import Either
import io.kotest.property.Arb
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.map

fun <E, A> Arb.Companion.either(arbE: Arb<E>, arbA: Arb<A>): Arb<Either<E, A>> {
    val arbLeft = arbE.map { Either.Left(it) }
    val arbRight = arbA.map { Either.Right(it) }
    return Arb.choice(arbLeft, arbRight)
}
