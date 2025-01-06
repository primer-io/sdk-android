package io.primer.android.clientToken.core.validation.data.repository

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.primer.android.clientToken.core.errors.data.exception.InvalidClientTokenException
import io.primer.android.clientToken.core.validation.data.datasource.ValidationTokenDataSource
import io.primer.android.clientToken.core.validation.data.model.TokenCheckStatusDataResponse
import io.primer.android.clientToken.core.validation.domain.repository.ValidateClientTokenRepository
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ValidateClientTokenDataRepositoryTest {
    private lateinit var configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>
    private lateinit var validateDataSource: ValidationTokenDataSource
    private lateinit var validateClientTokenRepository: ValidateClientTokenRepository

    @BeforeEach
    fun setUp() {
        configurationDataSource = mockk()
        validateDataSource = mockk()
        validateClientTokenRepository = ValidateClientTokenDataRepository(configurationDataSource, validateDataSource)
    }

    @Test
    fun `validate should return success when token is valid`() =
        runTest {
            // Arrange
            val clientToken = "valid_token"
            val config =
                mockk<ConfigurationData> {
                    every { pciUrl } returns "https://example.com"
                }
            val validationResponse = TokenCheckStatusDataResponse(success = true)

            coEvery { configurationDataSource.get() } returns config
            coEvery {
                validateDataSource.execute(any())
            } returns validationResponse

            // Act
            val result = validateClientTokenRepository.validate(clientToken)

            // Assert
            assertTrue(result.isSuccess)
            assertTrue(result.getOrNull()!!)
        }

    @Test
    fun `validate should throw InvalidClientTokenException when token is invalid`() =
        runTest {
            // Arrange
            val clientToken = "invalid_token"
            val config =
                mockk<ConfigurationData> {
                    every { pciUrl } returns "https://example.com"
                }
            val validationResponse = TokenCheckStatusDataResponse(success = null)

            coEvery { configurationDataSource.get() } returns config
            coEvery {
                validateDataSource.execute(any())
            } returns validationResponse

            // Act
            val result = validateClientTokenRepository.validate(clientToken)

            // Assert
            assertTrue(result.isFailure)
            assertThrows<InvalidClientTokenException> { result.getOrThrow() }
        }

    @Test
    fun `validate should throw exception when configuration data source fails`() =
        runTest {
            // Arrange
            val clientToken = "valid_token"

            coEvery { configurationDataSource.get() } throws IllegalStateException("Config error")

            // Act
            val result = validateClientTokenRepository.validate(clientToken)

            // Assert
            assertTrue(result.isFailure)
            assertThrows<IllegalStateException> { result.getOrThrow() }
        }
}
