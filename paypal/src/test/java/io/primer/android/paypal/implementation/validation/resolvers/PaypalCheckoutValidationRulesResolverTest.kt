package io.primer.android.paypal.implementation.validation.resolvers

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class PaypalCheckoutValidationRulesResolverTest {
    @MockK
    lateinit var orderTokenRule: PaypalValidOrderTokenRule

    private lateinit var resolver: PaypalCheckoutValidationRulesResolver

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        resolver = PaypalCheckoutValidationRulesResolver(orderTokenRule)
    }

    @Test
    fun `resolve should return a validation rules chain with the correct rule`() {
        val rulesChain = resolver.resolve()

        assertEquals(1, rulesChain.rules.size)
        assertEquals(orderTokenRule, rulesChain.rules[0])
    }
}
