package typeclasses

public fun interface Semigroup<A> {
    public fun A.combine(b: A): A
}
