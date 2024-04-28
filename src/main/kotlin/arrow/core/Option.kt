package arrow.core

public sealed class Option<out A> {
}

public object None : Option<Nothing>() {
    override fun toString(): String = "Option.None"
}

public data class Some<out T>(val value: T) : Option<T>() {
    override fun toString(): String = "Option.Some($value)"

    public companion object
}
