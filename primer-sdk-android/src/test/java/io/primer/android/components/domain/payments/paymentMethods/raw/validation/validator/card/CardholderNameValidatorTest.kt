package io.primer.android.components.domain.payments.paymentMethods.raw.validation.validator.card

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class CardholderNameValidatorTest {

    @Test
    fun `validate should not return error when cardholder name is valid`() = runTest {
        val resultError = CardholderNameValidator().run {
            validate("Test")
        }
        assertEquals(null, resultError)
    }

    @Test
    fun `validate should return error 'invalid-cardholder-name' when cardholder name is empty`() = runTest {
        val resultError = CardholderNameValidator().run {
            validate("")
        }
        assertEquals("invalid-cardholder-name", resultError?.errorId)
    }

    @Test
    fun `validate should return error 'invalid-cardholder-name' when cardholder name is blank`() = runTest {
        val resultError = CardholderNameValidator().run {
            validate("   ")
        }
        assertEquals("invalid-cardholder-name", resultError?.errorId)
    }

    @Test
    fun `validate should return error 'invalid-cardholder-name' when cardholder name is null`() = runTest {
        val resultError = CardholderNameValidator().run {
            validate(null)
        }
        assertEquals("invalid-cardholder-name", resultError?.errorId)
    }
}
