package arrow.typeclasses

public const val SemigroupDeprecation: String =
    "Semigroup is being deprecated, use combine (A, A) -> A lambdas or method references instead."

@Deprecated(SemigroupDeprecation)
public fun interface Semigroup<A> {
    public fun A.combine(b: A): A
}

@Deprecated(SemigroupDeprecation)
public fun <A> Semigroup<A>.combine(a: A, b: A): A =
    a.combine(b)
