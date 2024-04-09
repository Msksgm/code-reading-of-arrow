@file:OptIn(ExperimentalContracts::class)

package arrow.core

import arrow.typeclasses.Monoid
import arrow.typeclasses.Semigroup
import arrow.typeclasses.combine
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun main(args: Array<String>) {
    val left: Either<String, Int> = Either.Left("foo")
    val right: Either<String, Int> = Either.Right(1)

    println("left $left")
    println("left.isRight() ${left.isRight()}")
    println("left.isLeft() ${left.isLeft()}")

    println("right $right")
    println("right.isRight() ${right.isRight()}")
    println("right.isLeft() ${right.isLeft()}")
}

public sealed class Either<out A, out B> {
    @Deprecated(
        RedundantAPI + "Use isRight()",
        ReplaceWith("isRight()")
    )
    internal abstract val isRight: Boolean

    @Deprecated(
        RedundantAPI + "Use isLeft()",
        ReplaceWith("isLeft()")
    )
    internal abstract val isLeft: Boolean

    public fun isLeft(): Boolean {
        contract {
            returns(true) implies (this@Either is Left<A>)
            returns(false) implies (this@Either is Right<B>)
        }
        return this@Either is Left<A>
    }

    public fun isRight(): Boolean {
        contract {
            returns(true) implies (this@Either is Right<B>)
            returns(false) implies (this@Either is Left<A>)
        }
        return this@Either is Right<B>
    }

    public data class Left<out A> constructor(val value: A): Either<A, Nothing>() {
        override val isLeft = true
        override val isRight = false

        override fun toString(): String = "Either.Left($value)"

        public companion object {
            @Deprecated("Unused, will be removed from bytecode in Arrow 2.x.x", ReplaceWith("Left(Unit)"))
            @PublishedApi
            internal val leftUnit: Either<Unit, Nothing> = Left(Unit)
        }
    }

    /**
     * The right side of the disjoint union, as opposed to the [Left] side.
     */
    public data class Right<out B> constructor(val value: B) : Either<Nothing, B>() {
        override val isLeft = false
        override val isRight = true

        override fun toString(): String = "Either.Right($value)"

        public companion object {
            @PublishedApi
            internal val unit: Either<Nothing, Unit> = Right(Unit)
        }
    }

    @Deprecated(
        "tapLeft is being renamed to onLfet to be more consistent with the Kotlin Standard Library naming",
        ReplaceWith("onLeft(f)")
    )
    public inline fun tapLeft(f: (left: A) -> Unit): Either<A, B> = onLeft(f)

    @Deprecated(
        "tap is being renamed to onRight to be more consistent with the Kotlin Standard Library naming",
        ReplaceWith("onRight(f)")
    )
    public inline fun tap(f: (right: B) -> Unit): Either<A, B> = onRight(f)

    /**
     * Performs the given [action] on the encapsulated [B] value if this instance represents [Either.Right].
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
     */
    public inline fun onRight(action: (right: B) -> Unit): Either<A, B> {
        contract {
            callsInPlace(action, InvocationKind.AT_MOST_ONCE)
        }
        return also { if (it.isRight()) action(it.value) }
    }

    public inline fun onLeft(action: (left: A) -> Unit): Either<A, B> {
        contract {
            callsInPlace(action, InvocationKind.AT_MOST_ONCE)
        }
        return also { if (it.isLeft()) action(it.value) }
    }

    @Deprecated(
        "orNull is being renamed to getOrNull to be more consistent with the Kotlin Standard Library naming",
        ReplaceWith("getOrNull()")
    )
    public fun orNull(): B? {
        contract {
            returns(null) implies (this@Either is Left<A>)
            returnsNotNull() implies (this@Either is Right<B>)
        }
        return fold({ null }, { it })
    }

    /**
     * Returns the unwrapped value [0] of [Either.Right] or `null` if it is [Either.Left]
     *
     * ```kotlin
     * import arrow.core.Either
     * import io.kotest.matchers.shouldBe
     *
     * fun test() {
     *   Either.Right(12).getOrNull() shouldBe 12
     *   Either.Left(12).getOrNull() shouldBe null
     * }
     * ```
     *
     * @return
     */
    public fun getOrNUll(): B? {
        contract {
            returns(null) implies (this@Either is Left<A>)
            returnsNotNull() implies (this@Either is Right<B>)
        }
        return getOrElse { null }
    }

    /**
     * Transform an [Either] into a value of [C].
     * Alternative to using `when` to fold an [Either] into a value [C].
     *
     * ```kotlin
     * import arrow.core.Either
     * import io.kotest.matchers.shouldBe
     * import io.kotest.assertions.fail
     *
     * fun test() {
     *   Either.Right(1)
     *     .fold({ fail("Cannot be left") }, { it + 1 }) shouldBe 2
     *
     *   Either.Left(RuntimeException("Boom!))
     *     .fold({ -1 }, { fail("Cannot be right") }) shouldBe -1
     * }
     * ```
     *
     * @param ifLeft transform the [Either.Left] type [A] to [C]
     * @param ifRight transform the [Either.Right] type [B] to [C]
     * @return the transformed value [C] by applying [ifLeft] or [ifRight]
     */
    public inline fun <C> fold(ifLeft: (left: A) -> C, ifRight: (right: B) -> C): C {
        contract {
            callsInPlace(ifLeft, InvocationKind.AT_MOST_ONCE)
            callsInPlace(ifRight, InvocationKind.AT_MOST_ONCE)
        }
        return when (this) {
            is Left -> ifLeft(value)
            is Right -> ifRight(value)
        }
    }

    @Deprecated(
        "Use fold instead",
        ReplaceWith("fold(ifLeft, ifRight)")
    )
    public inline fun <C> foldLeft(initial: C, rightOperation: (C, B) -> C): C =
        fold({ initial }, { rightOperation(initial, it) })

    @Deprecated(
        NicheAPI + "Prefer when or fold instead",
        ReplaceWith("fold({ ifLeft }, f)")
    )
    public fun <C> foldMap(MN: Monoid<C>, f: (B) -> C): C =
        fold({ MN.empty() }, f)

    @Deprecated(
        NicheAPI + "Prefer when or fold instead",
        ReplaceWith("fold({ f(c, it) }, { g(c, it) })")
    )
    public inline fun <C> bifoldLeft(c: C, f: (C, A) -> C, g: (C, B) -> C): C =
        fold({ f(c, it) }, { g(c, it) })

    @Deprecated(
        NicheAPI + "Prefer when or fold instead",
        ReplaceWith("fold(f, g")
    )
    public inline fun <C> bifoldMap(MN: Monoid<C>, f: (A) -> C, g: (B) -> C) : C =
        fold(f, g)

    @Deprecated(
        "orNone is being renamed to getOrNone to be more consistent with the Kotlin Starndard Library naming",
        ReplaceWith("getOrNone()")
    )
    public fun orNone(): Option<B> = getOrNone()

    public fun getOrNone(): Option<B> = fold({ None }, { Some(it) })

    public companion object {

        @Deprecated(
            RedundantAPI + "Prefer Kotlin nullable syntax, or ensureNotNull inside Either DSL",
            ReplaceWith("a?.right() ?: Unit.left()")
        )
        @JvmStatic
        public fun <A> fromNullable(a: A?): Either<Unit, A> = a?.right() ?: Unit.left()
    }
}

public fun <A> A.left(): Either<A, Nothing> = Either.Left(this)

public fun <A> A.right(): Either<Nothing, A> = Either.Right(this)

public fun <A, B> Either<A, B>.combine(other: Either<A, B>, combineLeft: (A, A) -> A, combineRight: (B, B) -> B): Either<A, B> =
    when (val one = this){
        is Either.Left -> when (other) {
            is Either.Left -> Either.Left(combineLeft(one.value, other.value))
            is Either.Right -> one
        }

        is Either.Right -> when(other) {
            is Either.Left -> other
            is Either.Right -> Either.Right(combineRight(one.value, other.value))
        }
    }

@Deprecated(
    RedundantAPI + "Prefer zipOrAccumulate",
    ReplaceWith("Either.zipOrAccumulate<A, B, B, B, B>({ a:A, bb:A -> a + bb), this, b) { a:B, bb:B -> a + bb }")
)
public fun <A, B> Either<A, B>.combine(SGA: Semigroup<A>, SGB: Semigroup<B>, b: Either<A, B>): Either<A, B> =
    combine(b, SGA::combine, SGB::combine)

@Deprecated(
    RedundantAPI + "This API is overloaded with an API with a single argument",
    level = DeprecationLevel.HIDDEN
)
public inline fun <B> Either<*, B>.getOrElse(default: () -> B): B =
    fold({ default() }, ::identity)

/**
 * Get thr right value [B] of this [Either],
 * or compute a [default] value with the left value [A].
 *
 * ```kotlin
 * import arrow.core.Either
 * import arrow.core.getOrElse
 * import io.kotest.matchers.shouldBe
 *
 * fun test() {
 *   Either.Left(12) getOrElse { it + 5 } shouldBe 17
 * }
 * ```
 */
public inline infix fun <A, B> Either<A, B>.getOrElse(default: (A) -> B): B {
    contract { callsInPlace(default, InvocationKind.AT_MOST_ONCE)}
    return fold(default, ::identity)
}

/**
 * Returns the value from this [Right] or null if this is a [Left].
 *
 * Example:
 * ```kotlin
 * import arrow.core.Either.Right
 * import arrow.core.Either.Left
 *
 * fun main() {
 *   Right(12).orNUll() // Result: 12
 *   Left(12).orNull() // Result: null
 * }
 * ```
 */
@Deprecated(
    "Duplicated API. Please use Either's member function orNull. This will be removed towards Arrow 2.0",
    ReplaceWith("orNull()")
)
public fun <B> Either<*, B>.orNull(): B? =
    orNull()

public const val RedundantAPI: String =
    "This API is considered redundant. If this method is crucial for you, please let us know on the Arrow Github. Thanks!\n https://github.com/arrow-kt/arrow/issues\n"

public const val NicheAPI: String =
    "This API is niche and will be removed in the future. If this method is crucial for you, please"
