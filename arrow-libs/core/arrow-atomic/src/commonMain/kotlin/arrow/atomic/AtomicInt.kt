package arrow.atomic

public expect class AtomicInt(initialValue: Int) {
  public fun get(): Int
  public fun set(newValue: Int)
  public fun getAndSet(value: Int): Int
}

public var AtomicInt.value: Int
  get() = get()
  set(value) {
    set(value)
  }
