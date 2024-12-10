package io.primer.android.qrcode.implementation.configuration.data.repository

import io.mockk.every
import io.mockk.mockk
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.errors.data.exception.IllegalValueException
import io.primer.android.paymentmethods.core.errors.data.exception.AsyncIllegalValueKey
import io.primer.android.qrcode.implementation.configuration.domain.model.QrCodeConfigParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Locale

@ExperimentalCoroutinesApi
class QrCodeConfigurationDataRepositoryTest {

    private val configurationDataSource: CacheConfigurationDataSource = mockk()
    private val settings: PrimerSettings = mockk()
    private val qrCodeConfigurationDataRepository =
        QrCodeConfigurationDataRepository(configurationDataSource, settings)

    @Test
    fun `getPaymentMethodConfiguration should return QrCodeConfig when configuration is valid`() = runTest {
        val paymentMethodType = "qrCode"
        val paymentMethodConfigId = "1234"
        val locale = Locale.US

        val paymentMethod = mockk<PaymentMethodConfigDataResponse> {
            every { id } returns paymentMethodConfigId
            every { type } returns paymentMethodType
        }

        val configurationData = mockk<ConfigurationData>(relaxed = true) {
            every { paymentMethods } returns listOf(paymentMethod)
        }

        every { configurationDataSource.get() } returns configurationData
        every { settings.locale } returns locale

        val params = QrCodeConfigParams(paymentMethodType = paymentMethodType)
        val result = qrCodeConfigurationDataRepository.getPaymentMethodConfiguration(params).getOrThrow()

        assertEquals(paymentMethodConfigId, result.paymentMethodConfigId)
        assertEquals(locale.toLanguageTag(), result.locale)
    }

    @Test
    fun `getPaymentMethodConfiguration should throw AsyncIllegalValueKey when payment method is not found`() = runTest {
        val paymentMethodType = "qrCode"
        val locale = Locale.US

        val configurationData = mockk<ConfigurationData>(relaxed = true) {
            every { paymentMethods } returns emptyList()
        }

        every { configurationDataSource.get() } returns configurationData
        every { settings.locale } returns locale

        val params = QrCodeConfigParams(paymentMethodType = paymentMethodType)

        val result = qrCodeConfigurationDataRepository.getPaymentMethodConfiguration(params)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalValueException)
        val exception = result.exceptionOrNull() as IllegalValueException
        assertTrue(exception.key is AsyncIllegalValueKey)
    }
}
