package test.laws

import io.kotest.property.Arb
import io.kotest.property.PropertyContext
import io.kotest.property.checkAll
import test.Law
import test.LawSet
import test.equalUnderTheLaw

data class SemigroupLaws<F>(
    val name: String,
    val combine: (F, F) -> F,
    val G: Arb<F>,
    val eq: (F, F) -> Boolean = { a, b -> a == b }
): LawSet {
    override val laws: List<Law> =
        listOf(Law("Semigroup Laws ($name): associativity"){semigroupAssociate()})

    private suspend fun semigroupAssociate(): PropertyContext =
        checkAll(G, G, G) { A, B, C ->
            combine(combine(A, B), C).equalUnderTheLaw(combine(A, combine(B, C)), eq)
        }

    private suspend fun semigroupAssociative(): PropertyContext =
        checkAll(G, G, G) { A, B, C ->
            combine(combine(A, B), C).equalUnderTheLaw(combine(A, combine(B, C)), eq)
        }
}
