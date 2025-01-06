package io.primer.android.paymentmethods.core.configuration.domain

import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.paymentmethods.core.configuration.domain.model.PaymentMethodConfiguration
import io.primer.android.paymentmethods.core.configuration.domain.model.PaymentMethodConfigurationParams
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
internal class PaymentMethodConfigurationInteractorTest {
    @RelaxedMockK
    private lateinit var configurationRepository:
        PaymentMethodConfigurationRepository<PaymentMethodConfiguration, PaymentMethodConfigurationParams>

    private lateinit var configurationInteractor:
        PaymentMethodConfigurationInteractor<PaymentMethodConfiguration, PaymentMethodConfigurationParams>

    @BeforeEach
    fun setUp() {
        configurationInteractor =
            PaymentMethodConfigurationInteractor(
                configurationRepository = configurationRepository,
            )
    }

    @Test
    fun `PaymentMethodConfigurationInteractor invoke should return success when getPaymentMethodConfiguration returns a success result`() =
        runTest {
            // Given
            val paymentMethodConfiguration = mockk<PaymentMethodConfiguration>(relaxed = true)
            every { configurationRepository.getPaymentMethodConfiguration(any()) } returns
                Result.success(
                    paymentMethodConfiguration,
                )

            // When
            val paymentMethodConfigurationParams = mockk<PaymentMethodConfigurationParams>(relaxed = true)
            val result = configurationInteractor(paymentMethodConfigurationParams)

            // Then
            assertEquals(paymentMethodConfiguration, result.getOrThrow())
        }

    @Test
    fun `PaymentMethodConfigurationInteractor invoke should return failure when getPaymentMethodConfiguration returns a failure result`() =
        runTest {
            // Given
            val exception = mockk<Exception>(relaxed = true)
            every { configurationRepository.getPaymentMethodConfiguration(any()) } returns
                Result.failure(
                    exception,
                )

            // When, Then
            val paymentMethodConfigurationParams = mockk<PaymentMethodConfigurationParams>(relaxed = true)
            val result = configurationInteractor(paymentMethodConfigurationParams)
            assertThrows(Exception::class.java) {
                result.getOrThrow()
            }
        }
}
