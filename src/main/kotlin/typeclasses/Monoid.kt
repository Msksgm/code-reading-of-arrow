package typeclasses

@Deprecated("Monoid is being deprecated, use combine (A, A) -> A lambdas or method references with initial values instead.")
public interface Monoid<A> : Semigroup<A> {
  public fun empty(): A

  public companion object {
    @JvmStatic
    @JvmName("Integer")
    public fun int(): Monoid<Int> = IntMonoid

    private object IntMonoid : Monoid<Int> {
      override fun empty(): Int = 0
      override fun Int.combine(b: Int): Int = this + b
    }
  }
}
