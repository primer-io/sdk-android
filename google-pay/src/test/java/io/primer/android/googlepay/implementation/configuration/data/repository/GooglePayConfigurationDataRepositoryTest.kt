package io.primer.android.googlepay.implementation.configuration.data.repository

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.configuration.data.model.ClientSessionDataResponse
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.configuration.data.model.CountryCode
import io.primer.android.configuration.data.model.Environment
import io.primer.android.configuration.data.model.OrderDataResponse
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.configuration.data.model.PaymentMethodRemoteConfigOptions
import io.primer.android.configuration.domain.model.CheckoutModule
import io.primer.android.data.settings.PrimerGoogleShippingAddressParameters
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.errors.data.exception.IllegalValueException
import io.primer.android.googlepay.GooglePayFacade
import io.primer.android.googlepay.implementation.configuration.domain.model.GooglePayConfiguration
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.core.configuration.domain.model.NoOpPaymentMethodConfigurationParams
import io.primer.android.payments.core.utils.PaymentUtils
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Currency

internal class GooglePayConfigurationDataRepositoryTest {
    private lateinit var configurationDataSource: CacheConfigurationDataSource
    private lateinit var settings: PrimerSettings
    private lateinit var repository: GooglePayConfigurationDataRepository

    @BeforeEach
    fun setUp() {
        configurationDataSource = mockk()
        settings = mockk(relaxed = true)
        repository = GooglePayConfigurationDataRepository(configurationDataSource, settings)
    }

    @Test
    fun `getPaymentMethodConfiguration should return GooglePayConfiguration with valid data`() =
        runBlocking {
            // Given
            val paymentMethodRemoteConfigOptions =
                mockk<PaymentMethodRemoteConfigOptions> {
                    every { merchantId } returns "testMerchantId"
                }
            val paymentMethodConfig =
                mockk<PaymentMethodConfigDataResponse> {
                    every { type } returns PaymentMethodType.GOOGLE_PAY.name
                    every { options } returns paymentMethodRemoteConfigOptions
                }
            val orderResponse =
                mockk<OrderDataResponse> {
                    every { currentAmount } returns 1000
                    every { currencyCode } returns "USD"
                    every { countryCode } returns CountryCode.US
                }
            val clientSessionDataResponse =
                mockk<ClientSessionDataResponse> {
                    every { order } returns orderResponse
                    every { paymentMethod } returns
                        mockk {
                            every { orderedAllowedCardNetworks } returns emptyList()
                        }
                }

            val configurationData =
                mockk<ConfigurationData> {
                    every { paymentMethods } returns listOf(paymentMethodConfig)
                    every { clientSession } returns clientSessionDataResponse
                    every { environment } returns Environment.DEV
                }

            every {
                configurationDataSource.get()
            } returns configurationData
            val shippingOptions = mockk<CheckoutModule.Shipping>()
            every {
                configurationData.toConfiguration()
                    .checkoutModules
            } returns listOf(shippingOptions)
            every { settings.paymentMethodOptions.googlePayOptions.merchantName } returns "Test Merchant"
            every { settings.paymentMethodOptions.googlePayOptions.captureBillingAddress } returns true
            every { settings.paymentMethodOptions.googlePayOptions.existingPaymentMethodRequired } returns false
            val shippingAddressParameters = mockk<PrimerGoogleShippingAddressParameters>()
            every { settings.paymentMethodOptions.googlePayOptions.shippingAddressParameters } returns
                shippingAddressParameters

            mockkObject(PaymentUtils)
            every { PaymentUtils.minorToAmount(any<Int>(), any<Currency>()) } returns 10.00

            // When
            val result = repository.getPaymentMethodConfiguration(NoOpPaymentMethodConfigurationParams)

            // Then
            val expected =
                GooglePayConfiguration(
                    environment = GooglePayFacade.Environment.TEST,
                    gatewayMerchantId = "testMerchantId",
                    merchantName = "Test Merchant",
                    totalPrice = "10.0",
                    countryCode = "US",
                    currencyCode = "USD",
                    allowedCardNetworks = emptyList(),
                    allowedCardAuthMethods = listOf("PAN_ONLY", "CRYPTOGRAM_3DS"),
                    billingAddressRequired = true,
                    existingPaymentMethodRequired = false,
                    shippingOptions = shippingOptions,
                    shippingAddressParameters = shippingAddressParameters,
                    requireShippingMethod = false,
                    emailAddressRequired = false,
                )
            assertEquals(expected, result.getOrThrow())
        }

    @Test
    fun `getPaymentMethodConfiguration should return GooglePayConfiguration with filtered card networks`() =
        runBlocking {
            // Given
            val paymentMethodRemoteConfigOptions =
                mockk<PaymentMethodRemoteConfigOptions> {
                    every { merchantId } returns "testMerchantId"
                }
            val paymentMethodConfig =
                mockk<PaymentMethodConfigDataResponse> {
                    every { type } returns PaymentMethodType.GOOGLE_PAY.name
                    every { options } returns paymentMethodRemoteConfigOptions
                }
            val orderResponse =
                mockk<OrderDataResponse> {
                    every { currentAmount } returns 1000
                    every { currencyCode } returns "USD"
                    every { countryCode } returns CountryCode.US
                }

            val clientSessionDataResponse =
                mockk<ClientSessionDataResponse> {
                    every { order } returns orderResponse
                    every { paymentMethod } returns
                        mockk {
                            every { orderedAllowedCardNetworks } returns
                                listOf(
                                    CardNetwork.Type.VISA,
                                    CardNetwork.Type.MASTERCARD,
                                )
                        }
                }

            val configurationData =
                mockk<ConfigurationData> {
                    every { paymentMethods } returns listOf(paymentMethodConfig)
                    every { clientSession } returns clientSessionDataResponse
                    every { environment } returns Environment.DEV
                }

            every {
                configurationDataSource.get()
            } returns configurationData
            val shippingOptions = mockk<CheckoutModule.Shipping>()
            every {
                configurationData.toConfiguration()
                    .checkoutModules
            } returns listOf(shippingOptions)
            every { settings.paymentMethodOptions.googlePayOptions.merchantName } returns "Test Merchant"
            every { settings.paymentMethodOptions.googlePayOptions.captureBillingAddress } returns true
            every { settings.paymentMethodOptions.googlePayOptions.existingPaymentMethodRequired } returns false
            val shippingAddressParameters = mockk<PrimerGoogleShippingAddressParameters>()
            every { settings.paymentMethodOptions.googlePayOptions.shippingAddressParameters } returns
                shippingAddressParameters

            mockkObject(PaymentUtils)
            every { PaymentUtils.minorToAmount(any<Int>(), any<Currency>()) } returns 10.00

            // When
            val result = repository.getPaymentMethodConfiguration(NoOpPaymentMethodConfigurationParams)

            // Then
            val expected =
                GooglePayConfiguration(
                    environment = GooglePayFacade.Environment.TEST,
                    gatewayMerchantId = "testMerchantId",
                    merchantName = "Test Merchant",
                    totalPrice = "10.0",
                    countryCode = "US",
                    currencyCode = "USD",
                    allowedCardNetworks = listOf("VISA", "MASTERCARD"),
                    allowedCardAuthMethods = listOf("PAN_ONLY", "CRYPTOGRAM_3DS"),
                    billingAddressRequired = true,
                    existingPaymentMethodRequired = false,
                    shippingOptions = shippingOptions,
                    shippingAddressParameters = shippingAddressParameters,
                    requireShippingMethod = false,
                    emailAddressRequired = false,
                )
            assertEquals(expected, result.getOrThrow())
        }

    @Test
    fun `getPaymentMethodConfiguration should throw exception if merchantId is null`() =
        runBlocking {
            // Given
            val paymentMethodConfig =
                mockk<PaymentMethodConfigDataResponse> {
                    every { type } returns PaymentMethodType.GOOGLE_PAY.name
                    every { options } returns null
                }
            val orderResponse =
                mockk<OrderDataResponse> {
                    every { currentAmount } returns 1000
                    every { currencyCode } returns "USD"
                    every { countryCode } returns CountryCode.US
                }
            val clientSessionDataResponse =
                mockk<ClientSessionDataResponse> {
                    every { order } returns orderResponse
                    every { paymentMethod } returns
                        mockk {
                            every { orderedAllowedCardNetworks } returns emptyList()
                        }
                }
            val configurationData =
                mockk<ConfigurationData> {
                    every { paymentMethods } returns listOf(paymentMethodConfig)
                    every { clientSession } returns clientSessionDataResponse
                    every { environment } returns Environment.DEV
                }
            every {
                configurationDataSource.get()
            } returns configurationData
            val shippingOptions = mockk<CheckoutModule.Shipping>()
            every {
                configurationData.toConfiguration()
                    .checkoutModules
            } returns listOf(shippingOptions)
            every { settings.paymentMethodOptions.googlePayOptions.merchantName } returns "Test Merchant"
            every { settings.paymentMethodOptions.googlePayOptions.captureBillingAddress } returns true
            every { settings.paymentMethodOptions.googlePayOptions.existingPaymentMethodRequired } returns false

            // When
            val result = repository.getPaymentMethodConfiguration(NoOpPaymentMethodConfigurationParams)

            // Then
            val exception = result.exceptionOrNull()
            assert(exception is IllegalValueException)
        }
}
