package io.primer.android.ipay88.implementation.configuration.domain

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.core.InstantExecutorExtension
import io.primer.android.ipay88.implementation.configuration.domain.model.IPay88Config
import io.primer.android.ipay88.implementation.configuration.domain.model.IPay88ConfigParams
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
class DefaultIPay88ConfigurationInteractorTest {
    private lateinit var configurationRepository: PaymentMethodConfigurationRepository<
        IPay88Config,
        IPay88ConfigParams,
        >
    private lateinit var interactor: DefaultIPay88ConfigurationInteractor

    @BeforeEach
    fun setUp() {
        configurationRepository = mockk()
        interactor = DefaultIPay88ConfigurationInteractor(configurationRepository)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `performAction should return WebRedirectConfig when repository succeeds`() =
        runBlocking {
            // Arrange
            val params = IPay88ConfigParams("TEST_PAYMENT_METHOD")
            val expectedConfig = IPay88Config(paymentMethodConfigId = "12345", locale = "en-US")

            coEvery { configurationRepository.getPaymentMethodConfiguration(params) } returns
                Result.success(expectedConfig)

            // Act
            val result = interactor(params).getOrThrow()

            // Assert
            assertEquals(expectedConfig, result)
            coVerify { configurationRepository.getPaymentMethodConfiguration(params) }
        }

    @Test
    fun `performAction should throw exception when repository fails`() =
        runBlocking {
            // Arrange
            val params = IPay88ConfigParams(paymentMethodType = "TEST_PAYMENT_METHOD")
            val expectedException = Exception("Repository failed")

            coEvery {
                configurationRepository.getPaymentMethodConfiguration(params)
            } returns Result.failure(expectedException)

            // Act & Assert
            val exception =
                assertFailsWith<Exception> {
                    interactor(params).getOrThrow()
                }

            assertEquals(expectedException.message, exception.message)
            coVerify { configurationRepository.getPaymentMethodConfiguration(params) }
        }
}
