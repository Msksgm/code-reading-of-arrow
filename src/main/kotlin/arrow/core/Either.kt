@file:OptIn(ExperimentalContracts::class)

package arrow.core

import kotlin.contracts.ExperimentalContracts
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
}
