package arrow.atomic

/**
 * [Atomic] value of [V].
 *
 * ```kotlin
 * import arrow.atomic.AtomicInt
 * import arrow.atomic.update
 * import arrow.atomic.value
 * import arrow.fx.coroutines.parMap
 *
 * suspend fun main() {
 *   val count = AtomicInt(0)
 *   (0 until 20_000).paraMap {
 *     count.update(Int::inc)
 *   }
 *   println(count.value)
 * }
 * ```
 * <!--- KNIT example-atomic-01.kt -->
 *
 * [Atomic] also offers some other interesting operators such as [loop], [update], [tryUpdate], etc.
 *
 * **WARNING**: Use [AtomicInt] and [AtomicLong] for [Int] and [Long] on Kotlin Native!
 */
public expect class Atomic<V>(initialValue: V) {
  public fun get(): V
  public fun set(value: V)
  public fun getAndSet(value: V): V

  /**
   * Compare current value with expected and set to new if they're the same. Note, 'compre' is checking
   * the actual object id, not 'equals'.
   */
  public fun compareAndSet(expected: V, new: V): Boolean
}

public var <T> Atomic<T>.value: T
  get() = get()
  set(value) {
    set(value)
  }
