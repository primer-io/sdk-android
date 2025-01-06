package io.primer.android.webredirect.implementation.configuration.domain

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.webredirect.InstantExecutorExtension
import io.primer.android.webredirect.implementation.configuration.domain.model.WebRedirectConfig
import io.primer.android.webredirect.implementation.configuration.domain.model.WebRedirectConfigParams
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
class DefaultWebRedirectConfigurationInteractorTest {
    private lateinit var configurationRepository: PaymentMethodConfigurationRepository<
        WebRedirectConfig,
        WebRedirectConfigParams,
        >
    private lateinit var interactor: DefaultWebRedirectConfigurationInteractor

    @BeforeEach
    fun setUp() {
        configurationRepository = mockk()
        interactor = DefaultWebRedirectConfigurationInteractor(configurationRepository)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `performAction should return WebRedirectConfig when repository succeeds`() =
        runBlocking {
            // Arrange
            val params = WebRedirectConfigParams("TEST_PAYMENT_METHOD")
            val expectedConfig = WebRedirectConfig(paymentMethodConfigId = "12345", locale = "en-US")

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
            val params = WebRedirectConfigParams(paymentMethodType = "TEST_PAYMENT_METHOD")
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
