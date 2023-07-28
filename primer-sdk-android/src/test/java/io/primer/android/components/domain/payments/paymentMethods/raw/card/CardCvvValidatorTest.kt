package io.primer.android.components.domain.payments.paymentMethods.raw.card

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class CardCvvValidatorTest {

    @Test
    fun `validate should not return error when CVV is valid`() {
        val resultError = CardCvvValidator().run {
            validate(CvvData("333", "4242424242424242"))
        }
        Assertions.assertEquals(null, resultError)
    }

    @Test
    fun `validate should return error 'invalid-cvv' when CVV data is null`() {
        val resultError = CardCvvValidator().run {
            validate(null)
        }
        Assertions.assertEquals("invalid-cvv", resultError?.errorId)
    }

    @Test
    fun `validate should return error 'invalid-cvv' when CVV is blank`() {
        val resultError = CardCvvValidator().run {
            validate(CvvData("", "4242424242424242"))
        }
        Assertions.assertEquals("invalid-cvv", resultError?.errorId)
    }

    @Test
    fun `validate should return error 'invalid-cvv' when CVV size is not correct`() {
        val resultError = CardCvvValidator().run {
            validate(CvvData("23", "4242424242424242"))
        }
        Assertions.assertEquals("invalid-cvv", resultError?.errorId)
    }

    @Test
    fun `validate should return error 'invalid-cvv' when CVV has incorrect characters`() {
        val resultError = CardCvvValidator().run {
            validate(CvvData("23A", "4242424242424242"))
        }
        Assertions.assertEquals("invalid-cvv", resultError?.errorId)
    }
}
