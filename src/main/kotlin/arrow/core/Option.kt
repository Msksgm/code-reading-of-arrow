package arrow.core

public sealed class Option<out A> {

    @Deprecated(
        "Duplicated API. Please use Option's member function isNone. This will be removed towards Arrow 2.0",
        replaceWith = ReplaceWith("isNone()")
    )
    public abstract fun isEmpty(): Boolean
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
