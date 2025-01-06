package io.primer.android.paypal.implementation.validation.resolvers

import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class PaypalVaultValidationRulesResolverTest {
    @MockK
    lateinit var billingAgreementTokenRule: PaypalValidBillingAgreementTokenRule

    private lateinit var resolver: PaypalVaultValidationRulesResolver

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        resolver = PaypalVaultValidationRulesResolver(billingAgreementTokenRule)
    }

    @Test
    fun `resolve should return a validation rules chain with the correct rule`() {
        val rulesChain = resolver.resolve()

        assertEquals(1, rulesChain.rules.size)
        assertEquals(billingAgreementTokenRule, rulesChain.rules[0])
    }
}
