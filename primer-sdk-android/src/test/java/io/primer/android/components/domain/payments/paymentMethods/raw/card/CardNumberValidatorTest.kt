package io.primer.android.components.domain.payments.paymentMethods.raw.card

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class CardNumberValidatorTest {

    @Test
    fun `validate should not return error when card number is valid`() {
        val resultError = CardNumberValidator().run {
            validate("4242424242424242")
        }
        Assertions.assertEquals(null, resultError)
    }

    @Test
    fun `validate should return error 'invalid-card-number' when card number is blank`() {
        val resultError = CardNumberValidator().run {
            validate("")
        }
        Assertions.assertEquals("invalid-card-number", resultError?.errorId)
    }

    @Test
    fun `validate should return error 'invalid-card-number' when card number is null`() {
        val resultError = CardNumberValidator().run {
            validate(null)
        }
        Assertions.assertEquals("invalid-card-number", resultError?.errorId)
    }

    @Test
    fun `validate should return error 'invalid-card-number' when card number is invalid`() {
        val resultError = CardNumberValidator().run {
            validate("4242424242424")
        }
        Assertions.assertEquals("invalid-card-number", resultError?.errorId)
    }
}
