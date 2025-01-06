package io.primer.android.stripe.ach.implementation.configuration.data.repository

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.errors.data.exception.IllegalValueException
import io.primer.android.paymentmethods.core.errors.data.exception.AsyncIllegalValueKey
import io.primer.android.stripe.ach.implementation.configuration.domain.model.StripeAchConfigParams
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Locale
import kotlin.test.assertFailsWith

class StripeAchConfigurationDataRepositoryTest {
    @Test
    fun `getPaymentMethodConfiguration returns valid config`() =
        runTest {
            // Arrange
            val cacheConfigurationDataSource = mockk<CacheConfigurationDataSource>()
            val settings = mockk<PrimerSettings>()
            val params = StripeAchConfigParams(paymentMethodType = "STRIPE_ACH")

            val paymentMethodConfigData =
                mockk<PaymentMethodConfigDataResponse>().apply {
                    every { type } returns "STRIPE_ACH"
                    every { id } returns "12345"
                }

            coEvery { cacheConfigurationDataSource.get() } returns
                mockk {
                    every { paymentMethods } returns listOf(paymentMethodConfigData)
                }

            every { settings.locale } returns Locale.US
            val repository = StripeAchConfigurationDataRepository(cacheConfigurationDataSource, settings)

            // Act
            val result = repository.getPaymentMethodConfiguration(params)

            // Assert
            assertEquals("12345", result.getOrNull()?.paymentMethodConfigId)
            assertEquals(Locale.US, result.getOrNull()?.locale)
        }

    @Test
    fun `getPaymentMethodConfiguration throws exception when config is missing`() =
        runTest {
            // Arrange
            val cacheConfigurationDataSource = mockk<CacheConfigurationDataSource>()
            val settings = mockk<PrimerSettings>()
            val params = StripeAchConfigParams(paymentMethodType = "STRIPE_ACH")

            coEvery { cacheConfigurationDataSource.get() } returns
                mockk {
                    every { paymentMethods } returns emptyList()
                }

            val repository = StripeAchConfigurationDataRepository(cacheConfigurationDataSource, settings)

            // Act & Assert
            val exception =
                assertFailsWith<IllegalValueException> {
                    repository.getPaymentMethodConfiguration(params).getOrThrow()
                }

            assertEquals(AsyncIllegalValueKey.PAYMENT_METHOD_CONFIG_ID, exception.key)
        }
}
