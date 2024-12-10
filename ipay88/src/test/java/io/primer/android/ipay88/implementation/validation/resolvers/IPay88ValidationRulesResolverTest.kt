package io.primer.android.ipay88.implementation.validation.resolvers

import io.mockk.every
import io.mockk.mockk
import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.ipay88.implementation.validation.IPay88ValidationData
import io.primer.android.ipay88.implementation.validation.rules.ValidClientSessionAmountRule
import io.primer.android.ipay88.implementation.validation.rules.ValidClientSessionCountryCodeRule
import io.primer.android.ipay88.implementation.validation.rules.ValidClientSessionCurrencyRule
import io.primer.android.ipay88.implementation.validation.rules.ValidCustomerEmailRule
import io.primer.android.ipay88.implementation.validation.rules.ValidCustomerFirstNameRule
import io.primer.android.ipay88.implementation.validation.rules.ValidCustomerLastNameRule
import io.primer.android.ipay88.implementation.validation.rules.ValidProductDescriptionRule
import io.primer.android.ipay88.implementation.validation.rules.ValidRemarkRule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class IPay88ValidationRulesResolverTest {

    // Mock validation rules
    private val clientSessionAmountRule: ValidClientSessionAmountRule = mockk()
    private val clientSessionCurrencyRule: ValidClientSessionCurrencyRule = mockk()
    private val clientSessionCountryCodeRule: ValidClientSessionCountryCodeRule = mockk()
    private val productDescriptionRule: ValidProductDescriptionRule = mockk()
    private val customerFirstNameRule: ValidCustomerFirstNameRule = mockk()
    private val customerLastNameRule: ValidCustomerLastNameRule = mockk()
    private val customerEmailRule: ValidCustomerEmailRule = mockk()
    private val validRemarkRule: ValidRemarkRule = mockk()

    private val resolver = IPay88ValidationRulesResolver(
        clientSessionAmountRule,
        clientSessionCurrencyRule,
        clientSessionCountryCodeRule,
        productDescriptionRule,
        customerFirstNameRule,
        customerLastNameRule,
        customerEmailRule,
        validRemarkRule
    )

    @Test
    fun `resolve should return all the rules that enter the validation chain`() {
        // Mock validation data
        val validationData: IPay88ValidationData = mockk()

        // Mock behavior of validation rules
        every { clientSessionAmountRule.validate(validationData) } returns ValidationResult.Success
        every { clientSessionCurrencyRule.validate(validationData) } returns ValidationResult.Success
        every { clientSessionCountryCodeRule.validate(validationData) } returns ValidationResult.Success
        every { productDescriptionRule.validate(validationData) } returns ValidationResult.Success
        every { customerFirstNameRule.validate(validationData) } returns ValidationResult.Success
        every { customerLastNameRule.validate(validationData) } returns ValidationResult.Success
        every { customerEmailRule.validate(validationData) } returns ValidationResult.Success
        every { validRemarkRule.validate(validationData) } returns ValidationResult.Success

        // Invoke the method under test
        val resolvedRules = resolver.resolve()

        // Assert that the resolved rules contain all expected rules
        assertEquals(8, resolvedRules.rules.size)
        assertEquals(clientSessionAmountRule, resolvedRules.rules[0])
        assertEquals(clientSessionCurrencyRule, resolvedRules.rules[1])
        assertEquals(clientSessionCountryCodeRule, resolvedRules.rules[2])
        assertEquals(productDescriptionRule, resolvedRules.rules[3])
        assertEquals(customerFirstNameRule, resolvedRules.rules[4])
        assertEquals(customerLastNameRule, resolvedRules.rules[5])
        assertEquals(customerEmailRule, resolvedRules.rules[6])
        assertEquals(validRemarkRule, resolvedRules.rules[7])
    }
}
