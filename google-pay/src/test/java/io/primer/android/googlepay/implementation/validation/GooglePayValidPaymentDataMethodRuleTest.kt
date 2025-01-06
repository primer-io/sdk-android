package io.primer.android.googlepay.implementation.validation

import com.google.android.gms.wallet.PaymentData
import io.mockk.mockk
import io.primer.android.core.domain.validation.ValidationResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertIs

internal class GooglePayValidPaymentDataMethodRuleTest {
    private val validPaymentDataMethodRule = GooglePayValidPaymentDataMethodRule()

    @Test
    fun `validate should return Success when PaymentData is not null`() {
        // Given
        val paymentData = mockk<PaymentData>(relaxed = true)

        // When
        val result = validPaymentDataMethodRule.validate(paymentData)

        // Then
        assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun `validate should return Failure when PaymentData is null`() {
        // When
        val result = validPaymentDataMethodRule.validate(null)

        // Then
        assertIs<ValidationResult.Failure>(result)
    }
}
