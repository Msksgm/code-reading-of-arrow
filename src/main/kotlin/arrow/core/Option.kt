package arrow.core

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
public sealed class Option<out A> {

    @Deprecated(
        "Duplicated API. Please use Option's member function isNone. This will be removed towards Arrow 2.0",
        replaceWith = ReplaceWith("isNone()")
    )
    public abstract fun isEmpty(): Boolean

    /**
     * Returns a {Some<$B>] containing the result of applying $f to this $option's
     * value if this $option is nonempty. Otherwise return $none.
     *
     * @note This is similar to `flatMap` except here,
     * $f does not need to wrap its result in an $option.
     *
     * @param f the function to apply
     * @see flatMap
     */
    public inline fun <B> map(f: (A) -> B): Option<B> {
        contract { callsInPlace(f, InvocationKind.AT_MOST_ONCE) }
        return flatMap { Some(f(it)) }
    }

    /**
     * Returns the result of applying $f to this $option's value if
     * this $option is nonempty.
     * Returns $none if this $option is empty.
     * Slightly different from `map` in that $f is expected to
     * return an $option (which could be $none).
     *
     * @param f the function to apply
     * @see map
     */
    public inline fun <B> flatMap(f: (A) -> Option<B>): Option<B> {
        contract { callsInPlace(f, InvocationKind.AT_MOST_ONCE) }
        return when (this) {
            is None -> this
            is Some -> f(value)
        }
    }
}

public object None : Option<Nothing>() {
    @Deprecated(
        "Duplicated API. Please use Option's member function isNone. This will be removed towards Arrow 2.0",
        replaceWith = ReplaceWith("isNone()")
    )
    public override fun isEmpty(): Boolean = true

    override fun toString(): String = "Option.None"
}

public data class Some<out T>(val value: T) : Option<T>() {
    @Deprecated(
        "Duplicated API. Please use Option's member function isNone. This will be removed towards Arrow 2.0",
        replaceWith = ReplaceWith("isNone()")
    )
    public override fun isEmpty(): Boolean = false

    override fun toString(): String = "Option.Some($value)"

    public companion object {
        @PublishedApi
        @Deprecated("Unused, will be removed from bytecode in Arrow 2.x.x", ReplaceWith("Some(Unit)"))
        internal val unit: Option<Unit> = Some(Unit)
    }
}
