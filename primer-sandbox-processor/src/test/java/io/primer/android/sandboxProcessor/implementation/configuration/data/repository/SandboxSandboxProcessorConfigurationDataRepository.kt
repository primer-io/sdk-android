package io.primer.android.sandboxProcessor.implementation.configuration.data.repository

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.errors.data.exception.IllegalValueException
import io.primer.android.paymentmethods.core.errors.data.exception.AsyncIllegalValueKey
import io.primer.android.sandboxProcessor.implementation.configuration.domain.model.SandboxProcessorConfigParams
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Locale
import kotlin.test.assertFailsWith

class SandboxSandboxProcessorConfigurationDataRepository {
    @Test
    fun `getPaymentMethodConfiguration returns valid config`() = runTest {
        // Arrange
        val cacheConfigurationDataSource = mockk<CacheConfigurationDataSource>()
        val settings = mockk<PrimerSettings>()
        val params = SandboxProcessorConfigParams(paymentMethodType = "PRIMER_TEST_PAYPAL")

        val paymentMethodConfigData = mockk<PaymentMethodConfigDataResponse>().apply {
            every { type } returns "PRIMER_TEST_PAYPAL"
            every { id } returns "12345"
        }

        coEvery { cacheConfigurationDataSource.get() } returns mockk {
            every { paymentMethods } returns listOf(paymentMethodConfigData)
        }

        every { settings.locale } returns Locale.US
        val repository = SandboxProcessorConfigurationDataRepository(cacheConfigurationDataSource, settings)

        // Act
        val result = repository.getPaymentMethodConfiguration(params)

        // Assert
        assertEquals("12345", result.getOrNull()?.paymentMethodConfigId)
        assertEquals(Locale.US.toLanguageTag(), result.getOrNull()?.locale)
    }

    @Test
    fun `getPaymentMethodConfiguration throws exception when config is missing`() = runTest {
        // Arrange
        val cacheConfigurationDataSource = mockk<CacheConfigurationDataSource>()
        val settings = mockk<PrimerSettings>()
        val params = SandboxProcessorConfigParams(paymentMethodType = "PRIMER_TEST_PAYPAL")

        coEvery { cacheConfigurationDataSource.get() } returns mockk {
            every { paymentMethods } returns emptyList()
        }

        val repository = SandboxProcessorConfigurationDataRepository(cacheConfigurationDataSource, settings)

        // Act & Assert
        val exception = assertFailsWith<IllegalValueException> {
            repository.getPaymentMethodConfiguration(params).getOrThrow()
        }

        assertEquals(AsyncIllegalValueKey.PAYMENT_METHOD_CONFIG_ID, exception.key)
    }
}
