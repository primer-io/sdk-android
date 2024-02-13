package io.primer.android.components.domain.payments.paymentMethods.nolPay.validation.validator

import io.mockk.every
import io.mockk.mockk
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayUnlinkOtpDataValidator
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayValidations.INVALID_OTP_CODE_ERROR_ID
import io.primer.android.components.manager.nolPay.unlinkCard.composable.NolPayUnlinkCollectableData
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class NolPayUnlinkOtpDataValidatorTest {

    @Test
    fun `validate should return error for blank OTP code`() {
        val validator = NolPayUnlinkOtpDataValidator()
        val data = mockk<NolPayUnlinkCollectableData.NolPayOtpData>()

        every { data.otpCode } returns ""

        runTest {
            val result = validator.validate(data)
            assert(result.isSuccess)
            val errors = result.getOrThrow()
            assertEquals(INVALID_OTP_CODE_ERROR_ID, errors[0].errorId)
            assertEquals("OTP code cannot be blank.", errors[0].description)
        }
    }

    @Test
    fun `validate should return error for invalid OTP code format`() {
        val validator = NolPayUnlinkOtpDataValidator()
        val data = mockk<NolPayUnlinkCollectableData.NolPayOtpData>()

        every { data.otpCode } returns "1234" // Invalid OTP code format

        runTest {
            val result = validator.validate(data)
            assert(result.isSuccess)
            val errors = result.getOrThrow()
            assertEquals(INVALID_OTP_CODE_ERROR_ID, errors[0].errorId)
            assertEquals("OTP code is not valid.", errors[0].description)
        }
    }

    @Test
    fun `validate should return empty list for valid OTP code`() {
        val validator = NolPayUnlinkOtpDataValidator()
        val data = mockk<NolPayUnlinkCollectableData.NolPayOtpData>()

        every { data.otpCode } returns "123456" // Valid OTP code

        runTest {
            val result = validator.validate(data)
            assert(result.isSuccess)
            val errors = result.getOrThrow()

            assertEquals(0, errors.size)
        }
    }
}
