package io.primer.android.googlepay.implementation.validation

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class GooglePayValidationRulesResolverTest {

    private lateinit var validPaymentDataMethodRule: GooglePayValidPaymentDataMethodRule
    private lateinit var validationRulesResolver: GooglePayValidationRulesResolver

    @BeforeEach
    fun setUp() {
        validPaymentDataMethodRule = mockk()
        validationRulesResolver = GooglePayValidationRulesResolver(validPaymentDataMethodRule)
    }

    @Test
    fun `resolve should add validPaymentDataMethodRule to the validation chain`() {
        // When
        val validationRulesChain = validationRulesResolver.resolve()

        // Then
        assertTrue(validationRulesChain.rules.contains(validPaymentDataMethodRule))
    }
}
