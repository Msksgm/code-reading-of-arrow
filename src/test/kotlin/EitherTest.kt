import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import test.either

class EitherTest : StringSpec({
    val ARB = Arb.either(Arb.string(), Arb.int())
})
