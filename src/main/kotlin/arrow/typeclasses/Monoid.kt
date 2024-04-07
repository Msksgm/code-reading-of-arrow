package arrow.typeclasses

import arrow.core.Either
import arrow.core.combine

public const val MonoidDeprecation: String =
  "Monoid is being deprecated, use combine (A, A) -> A lambdas or method references with initial values instead."

@Deprecated(MonoidDeprecation)
public interface Monoid<A> : Semigroup<A> {
  /**
   * A zero value for this A
   */
  public fun empty(): A

  public companion object {
    @JvmStatic
    @JvmName("Integer")
    public fun int(): Monoid<Int> = IntMonoid

    @JvmStatic
    public fun string(): Monoid<String> = StringMonoid

    @JvmStatic
    public fun <A, B> either(SGA: Semigroup<A>, MB: Monoid<B>): Monoid<Either<A, B>> =
      EitherMonoid(SGA, MB)

    private object IntMonoid : Monoid<Int> {
      override fun empty(): Int = 0
      override fun Int.combine(b: Int): Int = this + b
    }

    private object StringMonoid: Monoid<String> {
      override fun String.combine(b: String): String = "${this}$b"
      override fun empty(): String = ""
    }

    private class EitherMonoid<L, R>(
      private val SGOL: Semigroup<L>,
      private val MOR: Monoid<R>
    ) : Monoid<Either<L, R>> {
      override fun empty(): Either<L, R> = Either.Right(MOR.empty())

      override fun Either<L, R>.combine(b: Either<L, R>): Either<L, R> =
        combine(SGOL, MOR, b)
    }
  }
}
