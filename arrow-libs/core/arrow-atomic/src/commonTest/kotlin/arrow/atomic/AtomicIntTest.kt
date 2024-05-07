package arrow.atomic

import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class AtomicIntTest {
  @Test
  fun setGetSuccessful() = runTest {
    checkAll(Arb.int(), Arb.int()) { x, y ->
      val r = AtomicInt(x)
      r.value = y
      r.value shouldBe y
    }
  }
}
