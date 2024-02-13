package io.primer.android.components.domain.payments.paymentMethods.nolPay.validation.validator

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.primer.android.components.domain.payments.metadata.phone.exception.PhoneValidationException
import io.primer.android.components.domain.payments.metadata.phone.model.PhoneMetadata
import io.primer.android.components.domain.payments.metadata.phone.repository.PhoneMetadataRepository
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayLinkMobileNumberDataValidator
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayValidations
import io.primer.android.components.manager.nolPay.linkCard.composable.NolPayLinkCollectableData
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class NolPayLinkMobileNumberDataValidatorTest {

    @RelaxedMockK
    internal lateinit var phoneMetadataRepository: PhoneMetadataRepository

    private lateinit var validator: NolPayLinkMobileNumberDataValidator

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        validator = NolPayLinkMobileNumberDataValidator(phoneMetadataRepository)
    }

    @Test
    fun `validate should rethrow error when PhoneMetadataRepository getPhoneMetadata failed to validate input`() {
        val collectableData = mockk<NolPayLinkCollectableData.NolPayPhoneData>()
        val expectedException = mockk<Exception>(relaxed = true)

        every { collectableData.mobileNumber } returns ""

        coEvery { phoneMetadataRepository.getPhoneMetadata(any()) }.returns(
            Result.failure(expectedException)
        )

        val exception = assertThrows<Exception> {
            runTest {
                val result = validator.validate(collectableData)
                assert(result.isFailure)
                result.getOrThrow()
            }
        }
        assertEquals(expectedException, exception)

        coVerify { phoneMetadataRepository.getPhoneMetadata(any()) }
    }

    @Test
    fun `validate should return list of errors when PhoneMetadataRepository getPhoneMetadata was successful for invalid input`() {
        val collectableData = mockk<NolPayLinkCollectableData.NolPayPhoneData>()
        val exception = mockk<PhoneValidationException>(relaxed = true)

        every { collectableData.mobileNumber } returns ""
        coEvery { phoneMetadataRepository.getPhoneMetadata(any()) }.returns(
            Result.failure(exception)
        )

        runTest {
            val result = validator.validate(collectableData)
            assert(result.isSuccess)
            val errors = result.getOrThrow()
            assertEquals(NolPayValidations.INVALID_MOBILE_NUMBER_ERROR_ID, errors.first().errorId)
        }

        coVerify { phoneMetadataRepository.getPhoneMetadata(any()) }
    }

    @Test
    fun `validate should return empty list of errors when PhoneMetadataRepository getPhoneMetadata was successful for valid input`() {
        val collectableData = mockk<NolPayLinkCollectableData.NolPayPhoneData>()
        val phoneMetadata = mockk<PhoneMetadata>(relaxed = true)

        every { collectableData.mobileNumber } returns VALID_MOBILE_NUMBER
        coEvery { phoneMetadataRepository.getPhoneMetadata(any()) }.returns(
            Result.success(phoneMetadata)
        )

        runTest {
            val result = validator.validate(collectableData)
            assert(result.isSuccess)
            val errors = result.getOrThrow()
            assertEquals(0, errors.size)
        }

        coVerify { phoneMetadataRepository.getPhoneMetadata(any()) }
    }

    private companion object {
        const val VALID_MOBILE_NUMBER = "1234567890"
    }
}
