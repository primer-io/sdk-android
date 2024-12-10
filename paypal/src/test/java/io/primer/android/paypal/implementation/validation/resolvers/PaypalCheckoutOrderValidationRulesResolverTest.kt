package io.primer.android.paypal.implementation.validation.resolvers

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class PaypalCheckoutOrderValidationRulesResolverTest {

    @MockK
    lateinit var validOrderAmountRule: PaypalValidOrderAmountRule

    @MockK
    lateinit var validOrderCurrencyRule: PaypalValidOrderCurrencyRule

    private lateinit var resolver: PaypalCheckoutOrderValidationRulesResolver

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        resolver = PaypalCheckoutOrderValidationRulesResolver(validOrderAmountRule, validOrderCurrencyRule)
    }

    @Test
    fun `resolve should return a validation rules chain with the correct rules`() {
        val rulesChain = resolver.resolve()

        assertEquals(2, rulesChain.rules.size)
        assertEquals(validOrderAmountRule, rulesChain.rules[0])
        assertEquals(validOrderCurrencyRule, rulesChain.rules[1])
    }
}
