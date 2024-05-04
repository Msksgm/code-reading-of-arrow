package arrow.core

/**
 * `NonEmptyList` is a data type use in __Arrow__ to model ordered lists that guarantee to have at least one value.
 *
 * ## Constructing NonEmptyList
 *
 * A `NonEmptyList` guarantees the list always has at least 1 element
 *
 * ```kotlin
 * import arrow.core.nonEmptyListOf
 * import arrow.core.toNonEmptyListOrNull
 *
 * fun main() {
 *   println(nonEmptyListOf(1, 2, 3, 4, 5))
 *   println(listOf(1, 2, 3).toNonEmptyListOrNull())
 *   println(emptyLIst<Int>().toNonEmptyListOrNull())
 * }
 * ```
 * <!--- KNIT example-nonemptylist-01.kt -->
 * ```text
 * NonEmptyList(1, 2, 3, 4, 5)
 * NonEmptyList(1, 2, 3)
 * null
 * ```
 *
 *
 * @param A
 * @property head
 * @property tail
 */
public class NonEmptyList<out A>(
    public override val head: A,
    public val tail: List<A>
) : AbstractList<A>(), NonEmptyCollection<A> {
    private constructor(list: List<A>) : this(list[0], list.drop(1))

    override val size: Int =
        1 + tail.size

    public val all: List<A>
        get() = toList()

    public override fun get(index: Int): A {
        if (index < 0 || index >= size) throw IndexOutOfBoundsException("$index is not 1..${size - 1}")
        return if (index == 0) head else tail[index - 1]
    }

    override fun isEmpty(): Boolean = false
}

public fun <A> Iterable<A>.toNonEmptyListOrNull(): NonEmptyList<A>? {
    val iter = iterator()
    if (!iter.hasNext()) return null
    return NonEmptyList(iter.next(), Iterable { iter }.toList())
}
