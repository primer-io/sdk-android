package io.primer.android.stripe.ach.implementation.validation.resolvers

import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.primer.android.stripe.ach.implementation.validation.rules.ValidStripeMandateDataRule
import io.primer.android.stripe.ach.implementation.validation.rules.ValidStripePublishableKeyRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class StripeInitValidationRulesResolverTest {
    @MockK
    private lateinit var validStripePublishableKeyRule: ValidStripePublishableKeyRule

    @MockK
    private lateinit var validStripeMandateDataRule: ValidStripeMandateDataRule

    @InjectMockKs
    private lateinit var resolver: StripeInitValidationRulesResolver

    @Test
    fun `resolve() should return chain with rule`() {
        val chain = resolver.resolve()

        assertEquals(listOf(validStripePublishableKeyRule, validStripeMandateDataRule), chain.rules)
    }
}
