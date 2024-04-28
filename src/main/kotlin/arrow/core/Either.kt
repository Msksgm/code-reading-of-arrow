@file:OptIn(ExperimentalContracts::class)

package arrow.core

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public sealed class Either<out A, out B> {
    /**
     * Returns true if this is [Left], false otherwise.
     */
    public fun isLeft(): Boolean {
        contract {
            returns(true) implies (this@Either is Left<A>)
            returns(false) implies (this@Either is Right<B>)
        }
        return this@Either is Left<A>
    }

    /**
     * Transform on [Either] into a value of [C].
     * Alternative to using `when` to fold on [Either] into a value [C].
     *
     * ```kotlin
     * import arrow.core.Either
     * import io.kotest.matchers.shouldBe
     * import io.kotest.assertions.fail
     *
     * fun test() {
     *  Either.Right(1)
     *    .fold({ fail("Cannot be left") }, { it + 1 }) shouldBe 2
     *  Either.Left(RuntimeException("Boom!"))
     *    .fold({ -1 }, { fail("Cannot be right") }) shouldBe -1
     * }
     * ```
     * <!--- KNIT example-either-23.kt -->
     * <!--- TEST lines.isEmpty() -->
     *
     * @param ifLeft transform the [Either.Left] type [A] to [C].
     * @param ifRight transform the [Either.Right] type [B] to [C].
     * @return the transformed value [C] by applying [ifLeft] or [ifRight] to [A] or [B] respectively.
     */
    public inline fun <C> fold(ifLeft: (left: A) -> C, ifRight: (right: B) -> C): C {
        contract {
            callsInPlace(ifLeft, InvocationKind.AT_MOST_ONCE)
            callsInPlace(ifRight, InvocationKind.AT_MOST_ONCE)
        }
        return when (this) {
            is Right -> ifRight(value)
            is Left -> ifLeft(value)
        }
    }

    /**
     * Returns true if this is [Right], false otherwise.
     */
    public fun isRight(): Boolean {
        contract {
            returns(true) implies (this@Either is Right<B>)
            returns(false) implies (this@Either is Left<A>)
        }
        return this@Either is Right<B>
    }

    /**
     * The left side of this disjoint union, as opposed to the [Right] side.
     */
    public data class Left<out A> constructor(val value: A) : Either<A, Nothing>() {
        override fun toString(): String = "Either.Left($value)"

        public companion object
    }

    /**
     * The right side of the disjoint union, as opposed to the [Left] side.
     */
    public data class Right<out B> constructor(val value: B) : Either<Nothing, B>() {
        override fun toString(): String = "Either.Right($value)"

        public companion object {
            @PublishedApi
            internal val unit: Right<Unit> = Right(Unit)
        }
    }

    /**
     * Performs the given [action] on the encapsulated [B] value if this instance represents [EIther.Right].
     * Returns the original [Either] unchanged.
     *
     * ```kotlin
     * import arrow.core.Either
     * import io.kotest.matchers.shouldBe
     *
     * fun test() {
     *   Either.Right(1).onRight(::println) shouldBe Either.Right(1)
     * }
     * ```
     * <!--- KNIT example-either-27.kt -->
     * <!--- TEST lines.isEmpty() -->
     */
    public inline fun onRight(action: (right: B) -> Unit): Either<A, B> {
        contract {
            callsInPlace(action, InvocationKind.AT_MOST_ONCE)
        }
        return also { if (it.isRight()) action (it.value) }
    }

    /**
     * Performs the given [action] on the encapsulated [A] if this instance represents [Either.Left].
     * Returns the original [Either] unchanged.
     *
     * ```kotlin
     * import arrow.core.Either
     * import io.kotest.matchers.shouldBe
     *
     * fun test() {
     *   Either.Left(2).onLeft(::println) shouldBe Either.Left(2)
     * }
     * ```
     * <!--- KNIT example-either-28.kt -->
     * <!--- TEST lines.isEmpty() -->
     */
    public inline fun onLeft(action: (left: A) -> Unit): Either<A, B> {
        contract {
            callsInPlace(action, InvocationKind.AT_MOST_ONCE)
        }
        return also { if (it.isLeft()) action (it.value) }
    }

    /**
     * Returns th unwrapped value [B] of [Either.Right] or `null` if it is [Either.Left].
     *
     * ```kotlin
     * import arrow.core.Either
     * import io.kotest.matchers.shouldBe
     *
     * fun test() {
     *  Either.Right(12).getOrNull() shouldBe 12
     *  Either.Left(12).getOrNull() shouldBe null
     * }
     * ```
     * <!--- KNIT example-either-29.kt -->
     * <!--- TEST lines.isEmpty() -->
     */
    public fun getOrNull(): B? {
        contract {
            returns(null) implies (this@Either is Left<A>)
            returnsNotNull() implies (this@Either is Right<B>)
        }
        return getOrElse { null }
    }

    /**
     * Transforms [Either] into [Option],
     * where the encapsulated value [B] is wrapped in [Some] when this instance represents [Either.Right],
     * or [None] if it is [Either.Left].
     *
     * ```kotlin
     * import arrow.core.Either
     * import arrow.core.Some
     * import arrow.core.None
     *
     * fun test() {
     *   Either.Right(12).getOrNone() shouldBe Some(12)
     *   Either.Left(12).getOrNone() shouldBe None
     * }
     * ```
     * <!--- KNIT example-either-31.kt -->
     * <!--- TEST lines.isEmpty() -->
     */
    public fun getOrNone(): Option<B> = fold({ None }, { Some(it) })
}

/**
 * Get thr right value [B] of this [Either]],
 * of compute a [default] value with the left value [A].
 *
 * ```kotlin
 * import arrow.core.Either
 * import arrow.core.getOrElse
 * import io.kotest.matchers.shouldBe
 *
 * fun test() {
 *   Either.Left(12) get OrElse { it + 5 } shouldBe 17
 * }
 * ```
 *  <!--- KNIT example-either-32.kt -->
 *  <!--- TEST lines.isEmpty() -->
 */
public inline infix fun <A, B> Either<A, B>.getOrElse(default: (A) -> B): B {
    contract {
        callsInPlace(default, InvocationKind.AT_MOST_ONCE)
    }
    return fold({ default(it) }, ::identity)
}

/**
 * Combine two [Either] values.
 * If both are [Right] then combine both [B] values using [combineRight] or if both are [Left] then combine both [A] values using [combineLeft].
 * otherwise it returns the `this` or fallbacks to [other] in case `this` is [Left].
 */
public fun <A, B> Either<A, B>.combine(other: Either<A, B>, combineLeft: (A, A) -> A, combineRight: (B, B) -> B): Either<A, B> =
    when (val one = this) {
        is Either.Left -> when (other) {
            is Either.Left -> Either.Left(combineLeft(one.value, other.value))
            is Either.Right -> one
        }

        is Either.Right -> when (other) {
            is Either.Left -> other
            is Either.Right -> Either.Right(combineRight(one.value, other.value))
        }
    }
