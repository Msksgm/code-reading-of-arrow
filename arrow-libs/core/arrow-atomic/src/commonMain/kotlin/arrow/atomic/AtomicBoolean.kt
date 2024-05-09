package arrow.atomic

public class AtomicBoolean(value: Boolean) {
  private val inner = AtomicInt(value.toInt())

  public var value: Boolean
    get() = inner.value != 0
    set(value) {
      inner.value = value.toInt()
    }

  public fun Boolean.toInt(): Int =
    if (this) 1 else 0
}
