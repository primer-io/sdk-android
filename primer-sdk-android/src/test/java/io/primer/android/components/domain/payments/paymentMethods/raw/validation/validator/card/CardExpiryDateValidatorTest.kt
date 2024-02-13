package io.primer.android.components.domain.payments.paymentMethods.raw.validation.validator.card

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class CardExpiryDateValidatorTest {

    @Test
    fun `validate should not return error when expiry date is valid`() = runTest {
        val resultError = CardExpiryDateValidator().run {
            validate("12/2033")
        }
        Assertions.assertEquals(null, resultError)
    }

    @Test
    fun `validate should not return error when expiry date is valid and padded`() = runTest {
        val resultError = CardExpiryDateValidator().run {
            validate("1/2033")
        }
        Assertions.assertEquals(null, resultError)
    }

    @Test
    fun `validate should not return error 'invalid-expiry-date' when expiry date is blank`() = runTest {
        val resultError = CardExpiryDateValidator().run {
            validate("")
        }
        Assertions.assertEquals("invalid-expiry-date", resultError?.errorId)
    }

    @Test
    fun `validate should not return error 'invalid-expiry-date' when expiry date is null`() = runTest {
        val resultError = CardExpiryDateValidator().run {
            validate(null)
        }
        Assertions.assertEquals("invalid-expiry-date", resultError?.errorId)
    }

    @Test
    fun `validate should not return error 'invalid-expiry-date' when expiry date has invalid pattern`() = runTest {
        val resultError = CardExpiryDateValidator().run {
            validate("1233")
        }
        Assertions.assertEquals("invalid-expiry-date", resultError?.errorId)
    }

    @Test
    fun `validate should not return error 'invalid-expiry-date' when expiry date is in past`() = runTest {
        val resultError = CardExpiryDateValidator().run {
            validate("01/2000")
        }
        Assertions.assertEquals("invalid-expiry-date", resultError?.errorId)
    }
}
