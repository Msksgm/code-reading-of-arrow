package arrow.core

public interface NonEmptyCollection<out A> : Collection<A> {
    override fun isEmpty(): Boolean = false
    public val head: A
}
