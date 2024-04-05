package test.laws

import io.kotest.property.Arb
import io.kotest.property.arbitrary.int

fun Arb.Companion.intSmall(factor: Int = 1000): Arb<Int> =
    Arb.int((Int.MIN_VALUE / factor)..(Int.MAX_VALUE / 100000))
