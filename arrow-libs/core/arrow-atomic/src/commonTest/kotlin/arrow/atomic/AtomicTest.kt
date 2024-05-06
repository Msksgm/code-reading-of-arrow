package arrow.atomic

import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AtomicTest {
  @Test
  fun setGetSuccessful() = runTest {
    checkAll(Arb.string(), Arb.string()) { x, y ->
      val r = Atomic(x)
      r.value = y
      r.value shouldBe y
    }
  }
}
