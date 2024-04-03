@file:OptIn(ExperimentalContracts::class)

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

    public class Left<out A> constructor(val value: A): Either<A, Nothing>() {
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
}

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

public const val RedundantAPI: String =
    "This API is considered redundant. If this method is crucial for you, please let us know on the Arrow Github. Thanks!\n https://github.com/arrow-kt/arrow/issues\n"
