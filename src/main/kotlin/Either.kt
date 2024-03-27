@file:OptIn(ExperimentalContracts::class)

import kotlin.contracts.ExperimentalContracts
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
}

public const val RedundantAPI: String =
    "This API is considered redundant. If this method is crucial for you, please let us know on the Arrow Github. Thanks!\n https://github.com/arrow-kt/arrow/issues\n"
