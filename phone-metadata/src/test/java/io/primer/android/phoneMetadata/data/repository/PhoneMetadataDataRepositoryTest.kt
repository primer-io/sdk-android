package io.primer.android.phoneMetadata.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.phoneMetadata.data.datasource.RemotePhoneMetadataDataSource
import io.primer.android.phoneMetadata.data.model.PhoneMetadataResponse
import io.primer.android.phoneMetadata.domain.exception.PhoneValidationException
import io.primer.android.phoneMetadata.domain.model.PhoneMetadata
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PhoneMetadataDataRepositoryTest {
    private val configurationDataSource = mockk<CacheConfigurationDataSource>()
    private val remoteMetadataDataSource = mockk<RemotePhoneMetadataDataSource>()
    private val repository = PhoneMetadataDataRepository(configurationDataSource, remoteMetadataDataSource)

    @Test
    fun `getPhoneMetadata should throw PhoneValidationException when phoneNumber is blank`() =
        runTest {
            val result = repository.getPhoneMetadata("")
            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull() as PhoneValidationException
            assertEquals("Phone number cannot be blank.", exception.message)
        }

    @Test
    fun `getPhoneMetadata should return valid PhoneMetadata`() =
        runTest {
            val pciUrl = "https://example.com"
            val phoneNumber = "1234567890"
            val metadataResponse =
                PhoneMetadataResponse(
                    isValid = true,
                    countryCode = "US",
                    nationalNumber = "1234567890",
                )

            coEvery { configurationDataSource.get().pciUrl } returns pciUrl
            coEvery { remoteMetadataDataSource.execute(any()) } returns metadataResponse

            val result = repository.getPhoneMetadata(phoneNumber)

            assertEquals(Result.success(PhoneMetadata("US", "1234567890")), result)

            coVerify { configurationDataSource.get().pciUrl }
            coVerify {
                remoteMetadataDataSource.execute(
                    BaseRemoteHostRequest(
                        host = pciUrl,
                        data = phoneNumber,
                    ),
                )
            }
        }

    @Test
    fun `getPhoneMetadata should throw PhoneValidationException when metadataResponse is invalid`() =
        runTest {
            val pciUrl = "https://example.com"
            val phoneNumber = "1234567890"
            val metadataResponse =
                PhoneMetadataResponse(
                    isValid = false,
                    countryCode = null,
                    nationalNumber = null,
                )

            coEvery { configurationDataSource.get().pciUrl } returns pciUrl
            coEvery { remoteMetadataDataSource.execute(any()) } returns metadataResponse

            val result = repository.getPhoneMetadata(phoneNumber)
            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull() as PhoneValidationException
            assertEquals("Failed to parse phone number.", exception.message)

            coVerify { configurationDataSource.get().pciUrl }
            coVerify {
                remoteMetadataDataSource.execute(
                    BaseRemoteHostRequest(
                        host = pciUrl,
                        data = phoneNumber,
                    ),
                )
            }
        }
}
