@file:OptIn(ExperimentalContracts::class)

package arrow.core

import arrow.core.Either.Left
import arrow.core.Either.Right
import arrow.core.Either.Right.Companion.unit
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public typealias EitherNel<E, A> = Either<NonEmptyList<E>, A>

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
     * Swap the generic parameters [A] and [B] of this [Either].
     *
     * ```kotlin
     * import arrow.core.Either
     * import io.kotest.matchers.shouldBe
     *
     * fun test() {
     *   Either.Left("left").swap() shouldBe Either.Right("left")
     *   Either.Right("right").swap() shouldBe Either.Left("right")
     * }
     * ```
     * <!--- KNIT example-either-24.kt --
     * <!-- TEST lines.isEmpty() -->
     */
    public fun swap(): Either<B, A> =
        fold({ Right(it) }, { Left(it) })

    /**
     * Map, or transform, the right value [B] of this [Either] to a new value [C].
     *
     * ```kotlin
     * import arrow.core.Either
     * import io.kotest.matchers.shouldBe
     *
     * fun test() {
     *   Either.Right(12).map { _: Int -> "flower" } shouldBe Either.Right("flower")
     *   Either.Left(12).map { _: Nothing -> "flower" } shouldBe Either.Left(12)
     * }
     * ```
     * <!--- KNIT example-either-25.kt -->
     * <!--- TEST lines.isEmpty() -->
     */
    public inline fun <C> map(f: (right: B) -> C): Either<A, C> {
        contract {
            callsInPlace(f, InvocationKind.AT_MOST_ONCE)
        }
        return flatMap { Either.Right(f(it))}
    }

    /**
     * Map, or transform, the left value [A] of this [Either] to a new value [C].
     *
     * ```kotlin
     * import arrow.core.Either
     * import io.kotest.matchers.shouldBe
     *
     * fun test() {
     *   Either.Right(12).mapLeft { _: Nothing -> "flower" } shouldBe Either.Right(12)
     *   Either.Left(12).mapLeft { _: Int -> "flower" } shouldBe Either.Left("flower")
     * }
     * ```
     * <!--- KNIT example-either-26.kt -->
     * <!--- TEST lines.isEmpty() -->
     */
    public inline fun <C> mapLeft(f: (left: A) -> C): Either<C, B> {
        contract {
            callsInPlace(f, InvocationKind.AT_MOST_ONCE)
        }
        return fold({ Left(f(it)) }, { Right(it) })
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

    public companion object {
        public inline fun <E, A, B, C, D, EE, F, G, H, I, Z> zipOrAccumulate(
            combine: (E, E) -> E,
            a: Either<E, A>,
            b: Either<E, B>,
            c: Either<E, C>,
            d: Either<E, D>,
            e: Either<E, EE>,
            f: Either<E, F>,
            g: Either<E, G>,
            h: Either<E, H>,
            i: Either<E, I>,
            transform: (A, B, C, D, EE, F, G, H, I) -> Z,
        ): Either<E, Z> {
            contract { callsInPlace(transform, InvocationKind.AT_MOST_ONCE) }
            return zipOrAccumulate(combine, a, b, c, d, e, f, g, h, i, unit) { aa, bb, cc, dd, ee, ff, gg, hh, ii, _ ->
                transform(aa, bb, cc, dd, ee, ff, gg, hh, ii)
            }
        }

        @Suppress("DuplicatedCode")
        public inline fun <E, A, B, C, D, EE, F, G, H, I, J, Z> zipOrAccumulate(
            combine: (E, E) -> E,
            a: Either<E, A>,
            b: Either<E, B>,
            c: Either<E, C>,
            d: Either<E, D>,
            e: Either<E, EE>,
            f: Either<E, F>,
            g: Either<E, G>,
            h: Either<E, H>,
            i: Either<E, I>,
            j: Either<E, J>,
            transform: (A, B, C, D, EE, F, G, H, I, J) -> Z,
        ): Either<E, Z> {
            contract { callsInPlace(transform, InvocationKind.AT_MOST_ONCE) }
            return if (a is Right && b is Right && c is Right && d is Right && e is Right && f is Right && g is Right && h is Right && i is Right && j is Right) {
                Right(transform(a.value, b.value, c.value, d.value, e.value, f.value, g.value, h.value, i.value, j.value))
            } else {
                var accumulatedError: Any? = EmptyValue
                accumulatedError = if (a is Left) a.value else accumulatedError
                accumulatedError = if (b is Left) EmptyValue.combine(accumulatedError, b.value, combine) else accumulatedError
                accumulatedError = if (c is Left) EmptyValue.combine(accumulatedError, c.value, combine) else accumulatedError
                accumulatedError = if (d is Left) EmptyValue.combine(accumulatedError, d.value, combine) else accumulatedError
                accumulatedError = if (e is Left) EmptyValue.combine(accumulatedError, e.value, combine) else accumulatedError
                accumulatedError = if (f is Left) EmptyValue.combine(accumulatedError, f.value, combine) else accumulatedError
                accumulatedError = if (g is Left) EmptyValue.combine(accumulatedError, g.value, combine) else accumulatedError
                accumulatedError = if (h is Left) EmptyValue.combine(accumulatedError, h.value, combine) else accumulatedError
                accumulatedError = if (i is Left) EmptyValue.combine(accumulatedError, i.value, combine) else accumulatedError
                accumulatedError = if (j is Left) EmptyValue.combine(accumulatedError, j.value, combine) else accumulatedError

                @Suppress("UNCHECKED_CAST")
                (Left(accumulatedError as E))
            }
        }

        public inline fun <E, A, B, C, D, EE, F, G, H, I, Z> zipOrAccumulate(
            a: Either<E, A>,
            b: Either<E, B>,
            c: Either<E, C>,
            d: Either<E, D>,
            e: Either<E, EE>,
            f: Either<E, F>,
            g: Either<E, G>,
            h: Either<E, H>,
            i: Either<E, I>,
            transform: (A, B, C, D, EE, F, G, H, I) -> Z,
        ): Either<NonEmptyList<E>, Z> {
            contract { callsInPlace(transform, InvocationKind.AT_MOST_ONCE) }
            return zipOrAccumulate(a, b, c, d, e, f, g, h, i, unit) { aa, bb, cc, dd, ee, ff, gg, hh, ii, _ ->
                transform(aa, bb, cc, dd, ee, ff, gg, hh, ii)
            }
        }

        @Suppress("DuplicatedCode")
        public inline fun <E, A, B, C, D, EE, F, G, H, I, J, Z> zipOrAccumulate(
            a: Either<E, A>,
            b: Either<E, B>,
            c: Either<E, C>,
            d: Either<E, D>,
            e: Either<E, EE>,
            f: Either<E, F>,
            g: Either<E, G>,
            h: Either<E, H>,
            i: Either<E, I>,
            j: Either<E, J>,
            transform: (A, B, C, D, EE, F, G, H, I, J) -> Z,
        ): Either<NonEmptyList<E>, Z> {
            contract { callsInPlace(transform, InvocationKind.AT_MOST_ONCE) }
            return if (a is Right && b is Right && c is Right && d is Right && e is Right && f is Right && g is Right && h is Right && i is Right && j is Right) {
                Right(transform(a.value, b.value, c.value, d.value, e.value, f.value, g.value, h.value, i.value, j.value))
            } else {
                val list = buildList(9) {
                    if (a is Left) add(a.value)
                    if (b is Left) add(b.value)
                    if (c is Left) add(c.value)
                    if (d is Left) add(d.value)
                    if (e is Left) add(e.value)
                    if (f is Left) add(f.value)
                    if (g is Left) add(g.value)
                    if (h is Left) add(h.value)
                    if (i is Left) add(i.value)
                    if (j is Left) add(j.value)
                }
                Left(NonEmptyList(list[0], list.drop(1)))
            }
        }

        @JvmName("zipOrAccumulateNonEmptyList")
        public inline fun <E, A, B, C, D, EE, F, G, H, I, Z> zipOrAccumulate(
            a: EitherNel<E, A>,
            b: EitherNel<E, B>,
            c: EitherNel<E, C>,
            d: EitherNel<E, D>,
            e: EitherNel<E, EE>,
            f: EitherNel<E, F>,
            g: EitherNel<E, G>,
            h: EitherNel<E, H>,
            i: EitherNel<E, I>,
            transform: (A, B, C, D, EE, F, G, H, I) -> Z,
        ): EitherNel<E, Z> {
            contract { callsInPlace(transform, InvocationKind.AT_MOST_ONCE) }
            return zipOrAccumulate(a, b, c, d, e, f, g, h, i, unit) { aa, bb, cc, dd, ee, ff, gg, hh, ii, _ ->
                transform(aa, bb, cc, dd, ee, ff, gg, hh, ii)
            }
        }

        @Suppress("DuplicatedCode")
        @JvmName("zipOrAccumulateNonEmptyList")
        public inline fun <E, A, B, C, D, EE, F, G, H, I, J, Z> zipOrAccumulate(
            a: EitherNel<E, A>,
            b: EitherNel<E, B>,
            c: EitherNel<E, C>,
            d: EitherNel<E, D>,
            e: EitherNel<E, EE>,
            f: EitherNel<E, F>,
            g: EitherNel<E, G>,
            h: EitherNel<E, H>,
            i: EitherNel<E, I>,
            j: EitherNel<E, J>,
            transform: (A, B, C, D, EE, F, G, H, I, J) -> Z,
        ): EitherNel<E, Z> {
            contract { callsInPlace(transform, InvocationKind.AT_MOST_ONCE) }
            return if (a is Right && b is Right && c is Right && d is Right && e is Right && f is Right && g is Right && h is Right && i is Right && j is Right) {
                Right(transform(a.value, b.value, c.value, d.value, e.value, f.value, g.value, h.value, i.value, j.value))
            } else {
                val list = buildList {
                    if (a is Left) addAll(a.value)
                    if (b is Left) addAll(b.value)
                    if (c is Left) addAll(c.value)
                    if (d is Left) addAll(d.value)
                    if (e is Left) addAll(e.value)
                    if (f is Left) addAll(f.value)
                    if (g is Left) addAll(g.value)
                    if (h is Left) addAll(h.value)
                    if (i is Left) addAll(i.value)
                    if (j is Left) addAll(j.value)
                }
                Left(NonEmptyList(list[0], list.drop(1)))
            }
        }
    }
}

/**
 * Binds the given function across [Right], that is,
 * Map, or transform, thr right value [B] of this [Either] into a new [Either] with a right value of type [C].
 * Returns a new [Either] with either the original left value of type [A] or the newly transformed right value of type [C].
 *
 * @param f The function to bind across [Right].
 */
public inline fun <A, B, C> Either<A, B>.flatMap(f: (right: B) -> Either<A, C>): Either<A, C> {
    contract {
        callsInPlace(f, InvocationKind.AT_MOST_ONCE)
    }
    return when(this) {
        is Either.Right -> f(this.value)
        is Either.Left -> this
    }
}

public fun <A> A.left(): Either<A, Nothing> = Left(this)

public fun <A> A.right(): Either<Nothing, A> = Right(this)

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
