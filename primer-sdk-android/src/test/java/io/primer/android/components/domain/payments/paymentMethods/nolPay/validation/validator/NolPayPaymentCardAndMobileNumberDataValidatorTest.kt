package io.primer.android.components.domain.payments.paymentMethods.nolPay.validation.validator

import io.mockk.every
import io.mockk.mockk
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayPaymentCardAndMobileDataValidator
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayValidations.INVALID_CARD_NUMBER_ERROR_ID
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayValidations.INVALID_DIALLING_CODE_ERROR_ID
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayValidations.INVALID_MOBILE_NUMBER_ERROR_ID
import io.primer.android.components.manager.nolPay.payment.composable.NolPayPaymentCollectableData
import io.primer.nolpay.api.models.PrimerNolPaymentCard
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class NolPayPaymentCardAndMobileNumberDataValidatorTest {

    @Test
    fun `validate should return error for invalid card number`() {
        val validator = NolPayPaymentCardAndMobileDataValidator()
        val data = mockk<NolPayPaymentCollectableData.NolPayCardAndPhoneData>()

        every { data.nolPaymentCard } returns PrimerNolPaymentCard("")

        runTest {
            val errors = validator.validate(data)

            assertEquals(1, errors.size)
            assertEquals(
                INVALID_CARD_NUMBER_ERROR_ID,
                errors[0].errorId
            )
            assertEquals(
                "Card number cannot be blank.",
                errors[0].description
            )
        }
    }

    @Test
    fun `validate should return error for invalid dialing code`() {
        val validator = NolPayPaymentCardAndMobileDataValidator()
        val data = mockk<NolPayPaymentCollectableData.NolPayCardAndPhoneData>()

        every { data.mobileNumber } returns VALID_MOBILE_NUMBER // Valid mobile number
        every { data.phoneCountryDiallingCode } returns "abc" // Invalid dialing code
        every { data.nolPaymentCard } returns PrimerNolPaymentCard(VALID_CARD_NUMBER)

        runTest {
            val errors = validator.validate(data)

            assertEquals(1, errors.size)
            assertEquals(
                INVALID_DIALLING_CODE_ERROR_ID,
                errors[0].errorId
            )
            assertEquals(
                "Mobile number dialling code is not valid.",
                errors[0].description
            )
        }
    }

    @Test
    fun `validate should return error for invalid mobile number`() {
        val validator = NolPayPaymentCardAndMobileDataValidator()
        val data = mockk<NolPayPaymentCollectableData.NolPayCardAndPhoneData>()

        every { data.mobileNumber } returns "12345" // Invalid mobile number
        every { data.phoneCountryDiallingCode } returns VALID_DIALLING_CODE // Valid dialing code
        every { data.nolPaymentCard } returns PrimerNolPaymentCard(VALID_CARD_NUMBER)

        runTest {
            val errors = validator.validate(data)

            assertEquals(1, errors.size)
            assertEquals(
                INVALID_MOBILE_NUMBER_ERROR_ID,
                errors[0].errorId
            )
            assertEquals("Mobile number is not valid.", errors[0].description)
        }
    }

    @Test
    fun `validate should return empty list for valid data`() {
        val validator = NolPayPaymentCardAndMobileDataValidator()
        val data = mockk<NolPayPaymentCollectableData.NolPayCardAndPhoneData>()

        every { data.mobileNumber } returns VALID_MOBILE_NUMBER // Valid mobile number
        every { data.phoneCountryDiallingCode } returns VALID_DIALLING_CODE // Valid dialing code
        every { data.nolPaymentCard } returns PrimerNolPaymentCard(VALID_CARD_NUMBER)

        runTest {
            val errors = validator.validate(data)

            assertEquals(0, errors.size)
        }
    }

    private companion object {
        const val VALID_MOBILE_NUMBER = "1234567890"
        const val VALID_DIALLING_CODE = "+1"
        const val VALID_CARD_NUMBER = "03134567"
    }
}
