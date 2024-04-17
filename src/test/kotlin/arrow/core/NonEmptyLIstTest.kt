package arrow.core

import arrow.core.test.nonEmptyList
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

class NonEmptyLIstTest: StringSpec({
    "terable.toNonEmptyListOrNull should round trip" {
        checkAll(Arb.nonEmptyList(Arb.int())) { nonEmptyList ->
            nonEmptyList.all.toNonEmptyListOrNull().shouldNotBeNull() shouldBe nonEmptyList
        }
    }
})
