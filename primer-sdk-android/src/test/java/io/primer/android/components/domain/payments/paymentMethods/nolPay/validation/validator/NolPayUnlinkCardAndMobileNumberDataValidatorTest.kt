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
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayUnlinkCardAndMobileNumberDataValidator
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayValidations.INVALID_CARD_NUMBER_ERROR_ID
import io.primer.android.components.domain.payments.paymentMethods.nolpay.validation.validator.NolPayValidations.INVALID_MOBILE_NUMBER_ERROR_ID
import io.primer.android.components.manager.nolPay.unlinkCard.composable.NolPayUnlinkCollectableData
import io.primer.nolpay.api.models.PrimerNolPaymentCard
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class NolPayUnlinkCardAndMobileNumberDataValidatorTest {

    @RelaxedMockK
    internal lateinit var phoneMetadataRepository: PhoneMetadataRepository

    private lateinit var validator: NolPayUnlinkCardAndMobileNumberDataValidator

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        validator = NolPayUnlinkCardAndMobileNumberDataValidator(phoneMetadataRepository)
    }

    @Test
    fun `validate should return error for invalid card number`() {
        val collectableData = mockk<NolPayUnlinkCollectableData.NolPayCardAndPhoneData>()

        every { collectableData.nolPaymentCard } returns PrimerNolPaymentCard("")

        runTest {
            val result = validator.validate(collectableData)
            assert(result.isSuccess)
            val errors = result.getOrThrow()

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
    fun `validate should rethrow error when PhoneMetadataRepository getPhoneMetadata failed to validate input`() {
        val collectableData = mockk<NolPayUnlinkCollectableData.NolPayCardAndPhoneData>()
        val expectedException = mockk<Exception>(relaxed = true)

        every { collectableData.nolPaymentCard } returns PrimerNolPaymentCard(VALID_CARD_NUMBER)
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
        val collectableData = mockk<NolPayUnlinkCollectableData.NolPayCardAndPhoneData>()
        val exception = mockk<PhoneValidationException>(relaxed = true)

        every { collectableData.mobileNumber } returns ""
        every { collectableData.nolPaymentCard } returns PrimerNolPaymentCard(VALID_CARD_NUMBER)

        coEvery { phoneMetadataRepository.getPhoneMetadata(any()) }.returns(
            Result.failure(exception)
        )

        runTest {
            val result = validator.validate(collectableData)
            assert(result.isSuccess)
            val errors = result.getOrThrow()
            assertEquals(INVALID_MOBILE_NUMBER_ERROR_ID, errors.first().errorId)
        }

        coVerify { phoneMetadataRepository.getPhoneMetadata(any()) }
    }

    @Test
    fun `validate should return empty list of errors when PhoneMetadataRepository getPhoneMetadata was successful for valid input`() {
        val collectableData = mockk<NolPayUnlinkCollectableData.NolPayCardAndPhoneData>()
        val phoneMetadata = mockk<PhoneMetadata>(relaxed = true)

        every { collectableData.mobileNumber } returns VALID_MOBILE_NUMBER
        every { collectableData.nolPaymentCard } returns PrimerNolPaymentCard(VALID_CARD_NUMBER)

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
        const val VALID_CARD_NUMBER = "03134567"
    }
}
