package io.primer.android.ipay88.implementation.configuration.data.repository

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.ipay88.implementation.configuration.domain.model.IPay88ConfigParams
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class IPay88ConfigurationDataRepositoryTest {
    private lateinit var configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>
    private lateinit var settings: PrimerSettings
    private lateinit var repository: IPay88ConfigurationDataRepository

    @BeforeEach
    fun setUp() {
        configurationDataSource = mockk()
        settings = mockk()
        repository = IPay88ConfigurationDataRepository(configurationDataSource, settings)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `getPaymentMethodConfiguration should return WebRedirectConfig when payment method type is found`() =
        runBlocking {
            // Arrange
            val paymentMethodType = "TEST_PAYMENT_METHOD"
            val paymentMethodConfigId = "12345"
            val locale = Locale.ENGLISH

            val mockPaymentMethod =
                mockk<PaymentMethodConfigDataResponse> {
                    every { type } returns paymentMethodType
                    every { id } returns paymentMethodConfigId
                }

            val configData =
                mockk<ConfigurationData>(relaxed = true) {
                    every { paymentMethods } returns listOf(mockPaymentMethod)
                }

            val params = IPay88ConfigParams(paymentMethodType)

            every { configurationDataSource.get() } returns configData
            every { settings.locale } returns locale

            // Act
            val result = repository.getPaymentMethodConfiguration(params).getOrThrow()

            // Assert
            assertEquals(paymentMethodConfigId, result.paymentMethodConfigId)
            assertEquals(locale.toLanguageTag(), result.locale)
        }

    @Test
    fun `getPaymentMethodConfiguration should throw exception when payment method type is not found`(): Unit =
        runBlocking {
            // Arrange
            val paymentMethodType = "TEST_PAYMENT_METHOD"
            val params = IPay88ConfigParams(paymentMethodType)
            val configData = mockk<ConfigurationData>(relaxed = true)

            every { configurationDataSource.get() } returns configData

            // Act & Assert
            assertFailsWith<Exception> {
                repository.getPaymentMethodConfiguration(params).getOrThrow()
            }
        }
}
