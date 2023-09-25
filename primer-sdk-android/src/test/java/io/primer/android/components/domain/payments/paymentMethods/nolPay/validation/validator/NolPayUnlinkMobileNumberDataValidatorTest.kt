package io.primer.android.components.domain.payments.paymentMethods.nolPay.validation.validator

import io.mockk.every
import io.mockk.mockk
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayUnlinkMobileNumberDataValidator
import io.primer.android.components.manager.nolPay.unlinkCard.composable.NolPayUnlinkCollectableData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class NolPayUnlinkMobileNumberDataValidatorTest {

    @Test
    fun `validate should return error for blank mobile number`() {
        val validator = NolPayUnlinkMobileNumberDataValidator()
        val data = mockk<NolPayUnlinkCollectableData.NolPayPhoneData>()

        every { data.mobileNumber } returns ""
        every { data.phoneCountryDiallingCode } returns VALID_DIALLING_CODE // Valid dialing code

        runTest {
            val errors = validator.validate(data)

            assertEquals(1, errors.size)
            assertEquals(
                NolPayUnlinkMobileNumberDataValidator.INVALID_MOBILE_NUMBER_ERROR_ID,
                errors[0].errorId
            )
            assertEquals("Mobile number cannot be blank.", errors[0].description)
        }
    }

    @Test
    fun `validate should return error for invalid dialing code`() {
        val validator = NolPayUnlinkMobileNumberDataValidator()
        val data = mockk<NolPayUnlinkCollectableData.NolPayPhoneData>()

        every { data.mobileNumber } returns VALID_MOBILE_NUMBER // Valid mobile number
        every { data.phoneCountryDiallingCode } returns "abc" // Invalid dialing code

        runTest {
            val errors = validator.validate(data)

            assertEquals(1, errors.size)
            assertEquals(
                NolPayUnlinkMobileNumberDataValidator.INVALID_DIALLING_CODE_ERROR_ID,
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
        val validator = NolPayUnlinkMobileNumberDataValidator()
        val data = mockk<NolPayUnlinkCollectableData.NolPayPhoneData>()

        every { data.mobileNumber } returns "12345" // Invalid mobile number
        every { data.phoneCountryDiallingCode } returns VALID_DIALLING_CODE // Valid dialing code
        runTest {
            val errors = validator.validate(data)

            assertEquals(1, errors.size)
            assertEquals(
                NolPayUnlinkMobileNumberDataValidator.INVALID_MOBILE_NUMBER_ERROR_ID,
                errors[0].errorId
            )
            assertEquals("Mobile number is not valid.", errors[0].description)
        }
    }

    @Test
    fun `validate should return empty list for valid data`() {
        val validator = NolPayUnlinkMobileNumberDataValidator()
        val data = mockk<NolPayUnlinkCollectableData.NolPayPhoneData>()

        every { data.mobileNumber } returns VALID_MOBILE_NUMBER // Valid mobile number
        every { data.phoneCountryDiallingCode } returns VALID_DIALLING_CODE // Valid dialing code

        runTest {
            val errors = validator.validate(data)

            assertEquals(0, errors.size)
        }
    }

    private companion object {
        const val VALID_MOBILE_NUMBER = "1234567890"
        const val VALID_DIALLING_CODE = "+1"
    }
}
