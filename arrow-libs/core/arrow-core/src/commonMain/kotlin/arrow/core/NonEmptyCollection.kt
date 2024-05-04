package arrow.core

/**
 * Common interface for collections that always have
 * at least one element (available from [head]).
 */
public interface NonEmptyCollection<out A> : Collection<A> {
    override fun isEmpty(): Boolean = false
    public val head: A
}
