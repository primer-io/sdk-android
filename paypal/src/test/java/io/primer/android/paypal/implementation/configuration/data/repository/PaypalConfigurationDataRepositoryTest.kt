package io.primer.android.paypal.implementation.configuration.data.repository

import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.primer.android.PrimerSessionIntent
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.configuration.data.model.OrderDataResponse
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.utils.BaseDataProvider
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paypal.implementation.configuration.domain.model.PaypalConfig
import io.primer.android.paypal.implementation.configuration.domain.model.PaypalConfigParams
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PaypalConfigurationDataRepositoryTest {
    private lateinit var configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>
    private lateinit var applicationIdProvider: BaseDataProvider<String>
    private lateinit var repository: PaypalConfigurationDataRepository

    @BeforeEach
    fun setUp() {
        configurationDataSource = mockk(relaxed = true)
        applicationIdProvider = mockk(relaxed = true)
        repository = PaypalConfigurationDataRepository(configurationDataSource, applicationIdProvider)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getPaymentMethodConfiguration should return PaypalCheckoutConfiguration`() =
        runTest {
            // Arrange
            val orderId = "order123"
            val currencyCode = "USD"
            val paymentMethodConfigId = "paypalConfigId"

            val orderDataResponse =
                OrderDataResponse(
                    orderId = orderId,
                    currencyCode = currencyCode,
                    totalOrderAmount = 100,
                )
            val paymentMethodConfigDataResponse =
                mockk<PaymentMethodConfigDataResponse> {
                    every { type } returns PaymentMethodType.PAYPAL.name
                    every { id } returns paymentMethodConfigId
                }
            val config =
                mockk<ConfigurationData> {
                    every { clientSession } returns
                        mockk {
                            every { order } returns orderDataResponse
                        }
                    every { paymentMethods } returns listOf(paymentMethodConfigDataResponse)
                }

            val params = PaypalConfigParams(PrimerSessionIntent.CHECKOUT)
            every { configurationDataSource.get() } returns config
            every { applicationIdProvider.provide() } returns "app123"

            val uriSuccessMock = mockk<Uri>()
            val uriCancelMock = mockk<Uri>()
            val uriBuilder = mockk<Uri.Builder>()

            mockkConstructor(Uri.Builder::class).also {
                every { anyConstructed<Uri.Builder>().scheme(any()) } returns uriBuilder
                every { uriBuilder.authority(any()) } returns uriBuilder
                every { uriBuilder.appendPath(any()) } returns uriBuilder
                every { uriBuilder.build() } returnsMany listOf(uriSuccessMock, uriCancelMock)
                every { uriSuccessMock.toString() } returns "primer://requestor.app123/paypal/uuid/success"
                every { uriCancelMock.toString() } returns "primer://requestor.app123/paypal/uuid/cancel"
            }

            // Act
            val result = repository.getPaymentMethodConfiguration(params)

            // Assert
            val expectedBaseUri = "primer://requestor.app123/paypal/uuid"
            val expectedSuccessUrl = "$expectedBaseUri/success"
            val expectedCancelUrl = "$expectedBaseUri/cancel"

            val expectedConfig =
                PaypalConfig.PaypalCheckoutConfiguration(
                    paymentMethodConfigId = paymentMethodConfigId,
                    amount = 100,
                    currencyCode = currencyCode,
                    successUrl = expectedSuccessUrl,
                    cancelUrl = expectedCancelUrl,
                )
            assertEquals(expectedConfig, result.getOrNull())
        }

    @Test
    fun `getPaymentMethodConfiguration should return PaypalVaultConfiguration`() =
        runTest {
            // Arrange
            val paymentMethodConfigId = "paypalConfigId"

            val paymentMethodConfigDataResponse =
                mockk<PaymentMethodConfigDataResponse> {
                    every { type } returns PaymentMethodType.PAYPAL.name
                    every { id } returns paymentMethodConfigId
                }
            val config =
                mockk<ConfigurationData> {
                    every { paymentMethods } returns listOf(paymentMethodConfigDataResponse)
                }

            val params = PaypalConfigParams(PrimerSessionIntent.VAULT)
            every { configurationDataSource.get() } returns config
            every { applicationIdProvider.provide() } returns "app123"

            val uriSuccessMock = mockk<Uri>()
            val uriCancelMock = mockk<Uri>()
            val uriBuilder = mockk<Uri.Builder>()

            mockkConstructor(Uri.Builder::class).also {
                every { anyConstructed<Uri.Builder>().scheme(any()) } returns uriBuilder
                every { uriBuilder.authority(any()) } returns uriBuilder
                every { uriBuilder.appendPath(any()) } returns uriBuilder
                every { uriBuilder.build() } returnsMany listOf(uriSuccessMock, uriCancelMock)
                every { uriSuccessMock.toString() } returns "primer://requestor.app123/paypal/uuid/success"
                every { uriCancelMock.toString() } returns "primer://requestor.app123/paypal/uuid/cancel"
            }

            // Act
            val result = repository.getPaymentMethodConfiguration(params)

            // Assert
            val expectedBaseUri = "primer://requestor.app123/paypal/uuid"
            val expectedSuccessUrl = "$expectedBaseUri/success"
            val expectedCancelUrl = "$expectedBaseUri/cancel"

            val expectedConfig =
                PaypalConfig.PaypalVaultConfiguration(
                    paymentMethodConfigId = paymentMethodConfigId,
                    successUrl = expectedSuccessUrl,
                    cancelUrl = expectedCancelUrl,
                )
            assertEquals(expectedConfig, result.getOrThrow())
        }
}
