package io.primer.android.components.domain.payments.paymentMethods.nolPay.validation.validator

import io.mockk.every
import io.mockk.mockk
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayLinkOtpDataValidator
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayValidations.INVALID_OTP_CODE_ERROR_ID
import io.primer.android.components.manager.nolPay.linkCard.composable.NolPayLinkCollectableData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class NolPayLinkOtpDataValidatorTest {

    @Test
    fun `validate should return error for blank OTP code`() {
        val validator = NolPayLinkOtpDataValidator()
        val data = mockk<NolPayLinkCollectableData.NolPayOtpData>()

        every { data.otpCode } returns ""

        runTest {
            val errors = validator.validate(data)

            assertEquals(1, errors.size)
            assertEquals(INVALID_OTP_CODE_ERROR_ID, errors[0].errorId)
            assertEquals("OTP code cannot be blank.", errors[0].description)
        }
    }

    @Test
    fun `validate should return error for invalid OTP code format`() {
        val validator = NolPayLinkOtpDataValidator()
        val data = mockk<NolPayLinkCollectableData.NolPayOtpData>()

        every { data.otpCode } returns "1234" // Invalid OTP code format

        runTest {
            val errors = validator.validate(data)

            assertEquals(1, errors.size)
            assertEquals(INVALID_OTP_CODE_ERROR_ID, errors[0].errorId)
            assertEquals("OTP code is not valid.", errors[0].description)
        }
    }

    @Test
    fun `validate should return empty list for valid OTP code`() {
        val validator = NolPayLinkOtpDataValidator()
        val data = mockk<NolPayLinkCollectableData.NolPayOtpData>()

        every { data.otpCode } returns "123456" // Valid OTP code

        runTest {
            val errors = validator.validate(data)

            assertEquals(0, errors.size)
        }
    }
}
