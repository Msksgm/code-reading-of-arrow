package arrow.atomic

import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

public class AtomicBooleanTest {

  @Test
  fun setGetSuccessful() = runTest {
    checkAll(Arb.boolean(), Arb.boolean()) { x, y ->
      val r = AtomicBoolean(x)
      r.value = y
      r.value shouldBe y
    }
  }
}
