package io.primer.android.components.domain.payments.paymentMethods.raw.card

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class CardExpiryDateValidatorTest {

    @Test
    fun `validate should not return error when expiry date is valid`() {
        val resultError = CardExpiryDateValidator().run {
            validate("12/2033")
        }
        Assertions.assertEquals(null, resultError)
    }

    @Test
    fun `validate should not return error when expiry date is valid and padded`() {
        val resultError = CardExpiryDateValidator().run {
            validate("1/2033")
        }
        Assertions.assertEquals(null, resultError)
    }

    @Test
    fun `validate should not return error 'invalid-expiry-date' when expiry date is blank`() {
        val resultError = CardExpiryDateValidator().run {
            validate("")
        }
        Assertions.assertEquals("invalid-expiry-date", resultError?.errorId)
    }

    @Test
    fun `validate should not return error 'invalid-expiry-date' when expiry date is null`() {
        val resultError = CardExpiryDateValidator().run {
            validate(null)
        }
        Assertions.assertEquals("invalid-expiry-date", resultError?.errorId)
    }

    @Test
    fun `validate should not return error 'invalid-expiry-date' when expiry date has invalid pattern`() {
        val resultError = CardExpiryDateValidator().run {
            validate("1233")
        }
        Assertions.assertEquals("invalid-expiry-date", resultError?.errorId)
    }

    @Test
    fun `validate should not return error 'invalid-expiry-date' when expiry date is in past`() {
        val resultError = CardExpiryDateValidator().run {
            validate("01/2000")
        }
        Assertions.assertEquals("invalid-expiry-date", resultError?.errorId)
    }
}
