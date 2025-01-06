package io.primer.android.phoneNumber.implementation.validation.validator

import io.mockk.coEvery
import io.mockk.mockk
import io.primer.android.phoneMetadata.domain.exception.PhoneValidationException
import io.primer.android.phoneMetadata.domain.model.PhoneMetadata
import io.primer.android.phoneMetadata.domain.repository.PhoneMetadataRepository
import io.primer.android.phoneNumber.PrimerPhoneNumberData
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PhoneNumberValidatorTest {
    private val phoneMetadataRepository: PhoneMetadataRepository = mockk()
    private val phoneNumberValidator = PhoneNumberValidator(phoneMetadataRepository)

    @Test
    fun `validate should return empty list when phone metadata is valid`() =
        runTest {
            val phoneNumberData = PrimerPhoneNumberData(phoneNumber = "+351123456789")
            coEvery {
                phoneMetadataRepository.getPhoneMetadata(phoneNumberData.phoneNumber)
            } returns
                Result.success(
                    PhoneMetadata(
                        countryCode = "PT",
                        nationalNumber = "123456789",
                    ),
                )

            val result = phoneNumberValidator.validate(phoneNumberData)

            assertTrue(result.isSuccess)
            assertTrue(result.getOrNull()!!.isEmpty())
        }

    @Test
    fun `validate should return validation error when PhoneValidationException is thrown`() =
        runTest {
            val phoneNumberData = PrimerPhoneNumberData(phoneNumber = "+351123456789")
            val errorMessage = "Invalid phone number"
            coEvery {
                phoneMetadataRepository.getPhoneMetadata(phoneNumberData.phoneNumber)
            } returns Result.failure(PhoneValidationException(errorMessage))

            val result = phoneNumberValidator.validate(phoneNumberData)

            val errorsList = result.getOrNull() ?: emptyList()
            assertTrue(result.isSuccess)
            assertEquals(PhoneNumberValidations.INVALID_MOBILE_NUMBER_ERROR_ID, errorsList[0].errorId)
            assertEquals(errorMessage, errorsList[0].description)
        }

    @Test
    fun `validate should rethrow exception when non-PhoneValidationException is thrown`() =
        runTest {
            val phoneNumberData = PrimerPhoneNumberData(phoneNumber = "+351123456789")
            val unexpectedException = RuntimeException("Unexpected error")
            coEvery {
                phoneMetadataRepository.getPhoneMetadata(phoneNumberData.phoneNumber)
            } returns Result.failure(unexpectedException)

            val result = phoneNumberValidator.validate(phoneNumberData)

            assertTrue(result.isFailure)
        }
}
