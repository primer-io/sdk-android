package io.primer.android.otp.implementation.validation.validator

import io.mockk.MockKAnnotations
import io.primer.android.components.domain.error.PrimerValidationError
import io.primer.android.otp.PrimerOtpData
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OtpValidatorTest {
    private lateinit var otpValidator: OtpValidator

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        otpValidator = OtpValidator()
    }

    @Test
    fun `validate should return success with empty list when OTP is valid`() = runBlocking {
        val primerOtpData = PrimerOtpData(otp = "123456")

        val result = otpValidator.validate(primerOtpData)

        assertTrue(result.isSuccess)
        assertEquals(emptyList<PrimerValidationError>(), result.getOrNull())
    }

    @Test
    fun `validate should return success with validation error when OTP is invalid`() = runBlocking {
        val primerOtpData = PrimerOtpData(otp = "12345")

        val result = otpValidator.validate(primerOtpData)

        assertTrue(result.isSuccess)
        val errors = result.getOrNull()
        assertEquals(1, errors?.size)
        assertEquals(OtpValidations.INVALID_OTP_ERROR_ID, errors?.first()?.errorId)
        assertEquals("OTP should be six digits long.", errors?.first()?.description)
    }

    @Test
    fun `validate should return success with validation error when OTP contains non-digit characters`() = runBlocking {
        val primerOtpData = PrimerOtpData(otp = "12B456")

        val result = otpValidator.validate(primerOtpData)

        assertTrue(result.isSuccess)
        val errors = result.getOrNull()
        assertEquals(1, errors?.size)
        assertEquals(OtpValidations.INVALID_OTP_ERROR_ID, errors?.first()?.errorId)
        assertEquals("OTP should be six digits long.", errors?.first()?.description)
    }
}
