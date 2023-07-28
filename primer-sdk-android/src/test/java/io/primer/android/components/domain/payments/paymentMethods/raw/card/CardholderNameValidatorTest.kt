package io.primer.android.components.domain.payments.paymentMethods.raw.card

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class CardholderNameValidatorTest {

    @Test
    fun `validate should not return error when cardholder name is valid`() {
        val resultError = CardholderNameValidator().run {
            validate("Test")
        }
        Assertions.assertEquals(null, resultError)
    }

    @Test
    fun `validate should return error 'invalid-cardholder-name' when cardholder name is empty`() {
        val resultError = CardholderNameValidator().run {
            validate("")
        }
        Assertions.assertEquals("invalid-cardholder-name", resultError?.errorId)
    }

    @Test
    fun `validate should return error 'invalid-cardholder-name' when cardholder name is blank`() {
        val resultError = CardholderNameValidator().run {
            validate("   ")
        }
        Assertions.assertEquals("invalid-cardholder-name", resultError?.errorId)
    }

    @Test
    fun `validate should return error 'invalid-cardholder-name' when cardholder name is null`() {
        val resultError = CardholderNameValidator().run {
            validate(null)
        }
        Assertions.assertEquals("invalid-cardholder-name", resultError?.errorId)
    }
}
