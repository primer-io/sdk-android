package io.primer.android.klarna.implementation.session.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import io.primer.android.PrimerSessionIntent
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.configuration.data.model.CountryCode
import io.primer.android.configuration.data.model.CustomerDataResponse
import io.primer.android.configuration.data.model.OrderDataResponse
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.exception.HttpException
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.errors.data.exception.SessionCreateException
import io.primer.android.klarna.implementation.session.data.datasource.RemoteKlarnaCheckoutPaymentSessionDataSource
import io.primer.android.klarna.implementation.session.data.datasource.RemoteKlarnaVaultPaymentSessionDataSource
import io.primer.android.klarna.implementation.session.data.models.CreateCheckoutPaymentSessionDataRequest
import io.primer.android.klarna.implementation.session.data.models.CreateSessionDataResponse
import io.primer.android.klarna.implementation.session.data.models.CreateVaultPaymentSessionDataRequest
import io.primer.android.klarna.implementation.session.data.models.KlarnaSessionType
import io.primer.android.klarna.implementation.session.data.models.LocaleDataRequest
import io.primer.android.klarna.implementation.session.data.models.toKlarnaSession
import io.primer.android.klarna.implementation.session.domain.models.KlarnaSession
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class KlarnaSessionDataRepositoryTest {
    @MockK
    private lateinit var klarnaCheckoutPaymentSessionDataSource:
        RemoteKlarnaCheckoutPaymentSessionDataSource

    @MockK
    private lateinit var klarnaVaultPaymentSessionDataSource:
        RemoteKlarnaVaultPaymentSessionDataSource

    @MockK
    private lateinit var configurationDataSource: CacheConfigurationDataSource

    @MockK
    private lateinit var config: PrimerConfig

    @InjectMockKs
    private lateinit var repository: KlarnaSessionDataRepository

    @Test
    fun `createSession() should return response when data source call succeeds and payment method intent is VAULT`() =
        runTest {
            mockkStatic("io.primer.android.klarna.implementation.session.data.models.CreateSessionDataResponseKt")
            val paymentMethodId = "id"
            val paymentMethods =
                listOf(
                    mockk<PaymentMethodConfigDataResponse> {
                        every { type } returns PaymentMethodType.KLARNA.name
                        every { id } returns paymentMethodId
                    },
                )
            val customer =
                mockk<CustomerDataResponse> {
                    every { billingAddress } returns null
                    every { shippingAddress } returns null
                }

            val countryCode = CountryCode.AD
            val currency = "euro"
            val order =
                mockk<OrderDataResponse> {
                    every { this@mockk.countryCode } returns countryCode
                    every { this@mockk.currencyCode } returns currency
                }

            val coreUrl = "https://www.example.com"
            val configuration =
                mockk<ConfigurationData> {
                    every { this@mockk.coreUrl } returns coreUrl
                    every { this@mockk.paymentMethods } returns paymentMethods
                    every { this@mockk.clientSession.customer } returns customer
                    every { this@mockk.clientSession.order } returns order
                }

            every { configurationDataSource.get() } returns configuration
            every {
                config.settings.paymentMethodOptions.klarnaOptions.recurringPaymentDescription
            } returns "description"

            val languageTag = "en"
            every { config.settings.locale.toLanguageTag() } returns languageTag
            val response = mockk<CreateSessionDataResponse>()
            val klarnaSession = mockk<KlarnaSession>()
            every { response.toKlarnaSession() } returns klarnaSession
            coEvery { klarnaVaultPaymentSessionDataSource.execute(any()) } returns response

            val result =
                repository.createSession(surcharge = null, PrimerSessionIntent.VAULT).getOrThrow()

            coVerify {
                klarnaVaultPaymentSessionDataSource.execute(
                    BaseRemoteHostRequest(
                        host = coreUrl,
                        data =
                        CreateVaultPaymentSessionDataRequest(
                            paymentMethodConfigId = paymentMethodId,
                            sessionType = KlarnaSessionType.RECURRING_PAYMENT,
                            description = "description",
                            localeData =
                            LocaleDataRequest(
                                countryCode = countryCode,
                                currencyCode = currency,
                                localeCode = languageTag,
                            ),
                        ),
                    ),
                )
            }
            verify {
                response.toKlarnaSession()
            }
            assertEquals(klarnaSession, result)
            unmockkStatic("io.primer.android.klarna.implementation.session.data.models.CreateSessionDataResponseKt")
        }

    @Test
    fun `createSession() should return SessionCreateException when data source call throws client error HttpException and payment method intent is VAULT`() =
        runTest {
            val paymentMethodId = "id"
            val paymentMethods =
                listOf(
                    mockk<PaymentMethodConfigDataResponse> {
                        every { type } returns PaymentMethodType.KLARNA.name
                        every { id } returns paymentMethodId
                    },
                )
            val customer =
                mockk<CustomerDataResponse> {
                    every { billingAddress } returns null
                    every { shippingAddress } returns null
                }

            val countryCode = CountryCode.AD
            val currency = "euro"
            val order =
                mockk<OrderDataResponse> {
                    every { this@mockk.countryCode } returns countryCode
                    every { this@mockk.currencyCode } returns currency
                }

            val coreUrl = "https://www.example.com"
            val configuration =
                mockk<ConfigurationData> {
                    every { this@mockk.coreUrl } returns coreUrl
                    every { this@mockk.paymentMethods } returns paymentMethods
                    every { this@mockk.clientSession.customer } returns customer
                    every { this@mockk.clientSession.order } returns order
                }

            every { configurationDataSource.get() } returns configuration

            every {
                config.settings.paymentMethodOptions.klarnaOptions.recurringPaymentDescription
            } returns "description"

            val languageTag = "en"
            every { config.settings.locale.toLanguageTag() } returns languageTag
            val exception =
                mockk<HttpException> {
                    every { isClientError() } returns true
                    every { error.diagnosticsId } returns "diagnosticId"
                    every { error.description } returns "description"
                }
            coEvery { klarnaVaultPaymentSessionDataSource.execute(any()) } throws exception

            val result =
                repository.createSession(surcharge = null, PrimerSessionIntent.VAULT).exceptionOrNull()

            coVerify {
                klarnaVaultPaymentSessionDataSource.execute(
                    BaseRemoteHostRequest(
                        host = coreUrl,
                        data =
                        CreateVaultPaymentSessionDataRequest(
                            paymentMethodConfigId = paymentMethodId,
                            sessionType = KlarnaSessionType.RECURRING_PAYMENT,
                            description = "description",
                            localeData =
                            LocaleDataRequest(
                                countryCode = countryCode,
                                currencyCode = currency,
                                localeCode = languageTag,
                            ),
                        ),
                    ),
                )
            }
            assertInstanceOf(SessionCreateException::class.java, result)
            val casted = result as SessionCreateException
            assertEquals(PaymentMethodType.KLARNA.name, casted.paymentMethodType)
            assertEquals("diagnosticId", casted.diagnosticsId)
            assertEquals("description", casted.description)
        }

    @Test
    fun `createSession() should return exception when data source call throws client error and payment method intent is VAULT`() =
        runTest {
            val paymentMethodId = "id"
            val paymentMethods =
                listOf(
                    mockk<PaymentMethodConfigDataResponse> {
                        every { type } returns PaymentMethodType.KLARNA.name
                        every { id } returns paymentMethodId
                    },
                )
            val customer =
                mockk<CustomerDataResponse> {
                    every { billingAddress } returns null
                    every { shippingAddress } returns null
                }

            val countryCode = CountryCode.AD
            val currency = "euro"
            val order =
                mockk<OrderDataResponse> {
                    every { this@mockk.countryCode } returns countryCode
                    every { this@mockk.currencyCode } returns currency
                }

            val coreUrl = "https://www.example.com"
            val configuration =
                mockk<ConfigurationData> {
                    every { this@mockk.coreUrl } returns coreUrl
                    every { this@mockk.paymentMethods } returns paymentMethods
                    every { this@mockk.clientSession.customer } returns customer
                    every { this@mockk.clientSession.order } returns order
                }
            every { configurationDataSource.get() } returns configuration

            every {
                config.settings.paymentMethodOptions.klarnaOptions.recurringPaymentDescription
            } returns "description"

            val languageTag = "en"
            every { config.settings.locale.toLanguageTag() } returns languageTag
            val exception = Exception()
            coEvery { klarnaVaultPaymentSessionDataSource.execute(any()) } throws exception

            val result =
                repository.createSession(surcharge = null, PrimerSessionIntent.VAULT).exceptionOrNull()

            coVerify {
                klarnaVaultPaymentSessionDataSource.execute(
                    BaseRemoteHostRequest(
                        host = coreUrl,
                        data =
                        CreateVaultPaymentSessionDataRequest(
                            paymentMethodConfigId = paymentMethodId,
                            sessionType = KlarnaSessionType.RECURRING_PAYMENT,
                            description = "description",
                            localeData =
                            LocaleDataRequest(
                                countryCode = countryCode,
                                currencyCode = currency,
                                localeCode = languageTag,
                            ),
                        ),
                    ),
                )
            }
            assertEquals(exception, result)
        }

    @Test
    fun `createSession() should return response when surcharge is 0, data source call succeeds and payment method intent is CHECKOUT`() =
        runTest {
            mockkStatic("io.primer.android.klarna.implementation.session.data.models.CreateSessionDataResponseKt")
            val paymentMethodId = "id"
            val paymentMethods =
                listOf(
                    mockk<PaymentMethodConfigDataResponse> {
                        every { type } returns PaymentMethodType.KLARNA.name
                        every { id } returns paymentMethodId
                    },
                )

            val customer =
                mockk<CustomerDataResponse> {
                    every { billingAddress } returns null
                    every { shippingAddress } returns null
                }

            val countryCode = CountryCode.AD
            val currency = "euro"
            val totalOrderAmount = 1
            val lineItemDescription = "description"
            val lineItemUnitAmount = 1
            val lineItemId = "itemId"
            val lineItemQuantity = 1
            val lineItemDiscountAmount = 1
            val lineItemTaxAmount = 1
            val lineItemProductType = "productType"
            val lineItem =
                mockk<OrderDataResponse.LineItemDataResponse> {
                    every { description } returns lineItemDescription
                    every { unitAmount } returns lineItemUnitAmount
                    every { itemId } returns lineItemId
                    every { quantity } returns lineItemQuantity
                    every { discountAmount } returns lineItemDiscountAmount
                    every { taxAmount } returns lineItemTaxAmount
                    every { productType } returns lineItemProductType
                }

            val order =
                mockk<OrderDataResponse> {
                    every { this@mockk.countryCode } returns countryCode
                    every { this@mockk.currencyCode } returns currency
                    every { this@mockk.totalOrderAmount } returns totalOrderAmount
                    every { this@mockk.lineItems } returns listOf(lineItem)
                }

            val coreUrl = "https://www.example.com"
            val configuration =
                mockk<ConfigurationData> {
                    every { this@mockk.coreUrl } returns coreUrl
                    every { this@mockk.paymentMethods } returns paymentMethods
                    every { this@mockk.clientSession.customer } returns customer
                    every { this@mockk.clientSession.order } returns order
                }
            every { configurationDataSource.get() } returns configuration

            val languageTag = "en"
            every { config.settings.locale.toLanguageTag() } returns languageTag

            val response = mockk<CreateSessionDataResponse>()
            val klarnaSession = mockk<KlarnaSession>()
            every { response.toKlarnaSession() } returns klarnaSession
            coEvery { klarnaCheckoutPaymentSessionDataSource.execute(any()) } returns response

            val result =
                repository.createSession(surcharge = null, PrimerSessionIntent.CHECKOUT).getOrThrow()

            coVerify {
                klarnaCheckoutPaymentSessionDataSource.execute(
                    BaseRemoteHostRequest(
                        host = coreUrl,
                        data =
                        CreateCheckoutPaymentSessionDataRequest(
                            paymentMethodConfigId = paymentMethodId,
                            sessionType = KlarnaSessionType.ONE_OFF_PAYMENT,
                            totalAmount = totalOrderAmount,
                            localeData =
                            LocaleDataRequest(
                                countryCode = countryCode,
                                currencyCode = currency,
                                localeCode = languageTag,
                            ),
                            orderItems =
                            listOf(
                                CreateCheckoutPaymentSessionDataRequest.OrderItem(
                                    name = lineItemDescription,
                                    unitAmount = lineItemUnitAmount,
                                    reference = lineItemId,
                                    quantity = lineItemQuantity,
                                    discountAmount = lineItemDiscountAmount,
                                    productType = lineItemProductType,
                                    taxAmount = lineItemTaxAmount,
                                ),
                            ),
                            billingAddress = null,
                            shippingAddress = null,
                        ),
                    ),
                )
            }
            verify {
                response.toKlarnaSession()
            }
            assertEquals(klarnaSession, result)
            unmockkStatic("io.primer.android.klarna.implementation.session.data.models.CreateSessionDataResponseKt")
        }

    @Test
    fun `createSession() should return response when surcharge is 140, data source call succeeds and payment method intent is CHECKOUT`() =
        runTest {
            mockkStatic("io.primer.android.klarna.implementation.session.data.models.CreateSessionDataResponseKt")

            val paymentMethodId = "id"
            val paymentMethods =
                listOf(
                    mockk<PaymentMethodConfigDataResponse> {
                        every { type } returns PaymentMethodType.KLARNA.name
                        every { id } returns paymentMethodId
                    },
                )

            val customer =
                mockk<CustomerDataResponse> {
                    every { billingAddress } returns null
                    every { shippingAddress } returns null
                }

            val countryCode = CountryCode.AD
            val currency = "euro"
            val totalOrderAmount = 1
            val lineItemDescription = "description"
            val lineItemUnitAmount = 1
            val lineItemId = "itemId"
            val lineItemQuantity = 1
            val lineItemDiscountAmount = 1
            val lineItemTaxAmount = 1
            val lineItemProductType = "productType"
            val lineItem =
                mockk<OrderDataResponse.LineItemDataResponse> {
                    every { description } returns lineItemDescription
                    every { unitAmount } returns lineItemUnitAmount
                    every { itemId } returns lineItemId
                    every { quantity } returns lineItemQuantity
                    every { discountAmount } returns lineItemDiscountAmount
                    every { taxAmount } returns lineItemTaxAmount
                    every { productType } returns lineItemProductType
                }

            val order =
                mockk<OrderDataResponse> {
                    every { this@mockk.countryCode } returns countryCode
                    every { this@mockk.currencyCode } returns currency
                    every { this@mockk.totalOrderAmount } returns totalOrderAmount
                    every { this@mockk.lineItems } returns listOf(lineItem)
                }

            val coreUrl = "https://www.example.com"
            val configuration =
                mockk<ConfigurationData> {
                    every { this@mockk.coreUrl } returns coreUrl
                    every { this@mockk.paymentMethods } returns paymentMethods
                    every { this@mockk.clientSession.customer } returns customer
                    every { this@mockk.clientSession.order } returns order
                }

            every { configurationDataSource.get() } returns configuration

            val languageTag = "en"
            every { config.settings.locale.toLanguageTag() } returns languageTag

            val response = mockk<CreateSessionDataResponse>()
            val klarnaSession = mockk<KlarnaSession>()
            every { response.toKlarnaSession() } returns klarnaSession
            coEvery { klarnaCheckoutPaymentSessionDataSource.execute(any()) } returns response

            val result =
                repository.createSession(surcharge = 140, PrimerSessionIntent.CHECKOUT).getOrNull()

            coVerify {
                klarnaCheckoutPaymentSessionDataSource.execute(
                    BaseRemoteHostRequest(
                        host = coreUrl,
                        data =
                        CreateCheckoutPaymentSessionDataRequest(
                            paymentMethodConfigId = paymentMethodId,
                            sessionType = KlarnaSessionType.ONE_OFF_PAYMENT,
                            totalAmount = totalOrderAmount,
                            localeData =
                            LocaleDataRequest(
                                countryCode = countryCode,
                                currencyCode = currency,
                                localeCode = languageTag,
                            ),
                            orderItems =
                            listOf(
                                CreateCheckoutPaymentSessionDataRequest.OrderItem(
                                    name = lineItemDescription,
                                    unitAmount = lineItemUnitAmount,
                                    reference = lineItemId,
                                    quantity = lineItemQuantity,
                                    discountAmount = lineItemDiscountAmount,
                                    productType = lineItemProductType,
                                    taxAmount = lineItemTaxAmount,
                                ),
                                CreateCheckoutPaymentSessionDataRequest.OrderItem(
                                    name = "surcharge",
                                    unitAmount = 140,
                                    reference = null,
                                    quantity = 1,
                                    discountAmount = null,
                                    productType = "surcharge",
                                    taxAmount = null,
                                ),
                            ),
                            billingAddress = null,
                            shippingAddress = null,
                        ),
                    ),
                )
            }
            verify {
                response.toKlarnaSession()
            }
            assertEquals(klarnaSession, result)
            unmockkStatic("io.primer.android.klarna.implementation.session.data.models.CreateSessionDataResponseKt")
        }

    @Test
    fun `createSession() should return SessionCreateException when data source call throws client error HttpException and payment method intent is CHECKOUT`() =
        runTest {
            val paymentMethodId = "id"
            val paymentMethods =
                listOf(
                    mockk<PaymentMethodConfigDataResponse> {
                        every { type } returns PaymentMethodType.KLARNA.name
                        every { id } returns paymentMethodId
                    },
                )

            val customer =
                mockk<CustomerDataResponse> {
                    every { billingAddress } returns null
                    every { shippingAddress } returns null
                }

            val countryCode = CountryCode.AD
            val currency = "euro"
            val totalOrderAmount = 1
            val lineItemDescription = "description"
            val lineItemUnitAmount = 1
            val lineItemId = "itemId"
            val lineItemQuantity = 1
            val lineItemDiscountAmount = 1
            val lineItemTaxAmount = 1
            val lineItemProductType = "productType"
            val lineItem =
                mockk<OrderDataResponse.LineItemDataResponse> {
                    every { description } returns lineItemDescription
                    every { unitAmount } returns lineItemUnitAmount
                    every { itemId } returns lineItemId
                    every { quantity } returns lineItemQuantity
                    every { discountAmount } returns lineItemDiscountAmount
                    every { taxAmount } returns lineItemTaxAmount
                    every { productType } returns lineItemProductType
                }

            val order =
                mockk<OrderDataResponse> {
                    every { this@mockk.countryCode } returns countryCode
                    every { this@mockk.currencyCode } returns currency
                    every { this@mockk.totalOrderAmount } returns totalOrderAmount
                    every { this@mockk.lineItems } returns listOf(lineItem)
                }

            val coreUrl = "https://www.example.com"
            val configuration =
                mockk<ConfigurationData> {
                    every { this@mockk.coreUrl } returns coreUrl
                    every { this@mockk.paymentMethods } returns paymentMethods
                    every { this@mockk.clientSession.customer } returns customer
                    every { this@mockk.clientSession.order } returns order
                }

            every { configurationDataSource.get() } returns configuration

            val languageTag = "en"
            every { config.settings.locale.toLanguageTag() } returns languageTag

            val exception =
                mockk<HttpException> {
                    every { isClientError() } returns true
                    every { error.diagnosticsId } returns "diagnosticId"
                    every { error.description } returns "description"
                }
            coEvery { klarnaCheckoutPaymentSessionDataSource.execute(any()) } throws exception

            val result =
                repository.createSession(surcharge = null, PrimerSessionIntent.CHECKOUT)
                    .exceptionOrNull()

            coVerify {
                klarnaCheckoutPaymentSessionDataSource.execute(
                    BaseRemoteHostRequest(
                        host = coreUrl,
                        data =
                        CreateCheckoutPaymentSessionDataRequest(
                            paymentMethodConfigId = paymentMethodId,
                            sessionType = KlarnaSessionType.ONE_OFF_PAYMENT,
                            totalAmount = totalOrderAmount,
                            localeData =
                            LocaleDataRequest(
                                countryCode = countryCode,
                                currencyCode = currency,
                                localeCode = languageTag,
                            ),
                            orderItems =
                            listOf(
                                CreateCheckoutPaymentSessionDataRequest.OrderItem(
                                    name = lineItemDescription,
                                    unitAmount = lineItemUnitAmount,
                                    reference = lineItemId,
                                    quantity = lineItemQuantity,
                                    discountAmount = lineItemDiscountAmount,
                                    productType = lineItemProductType,
                                    taxAmount = lineItemTaxAmount,
                                ),
                            ),
                            billingAddress = null,
                            shippingAddress = null,
                        ),
                    ),
                )
            }
            assertInstanceOf(SessionCreateException::class.java, result)
            val casted = result as SessionCreateException
            assertEquals(PaymentMethodType.KLARNA.name, casted.paymentMethodType)
            assertEquals("diagnosticId", casted.diagnosticsId)
            assertEquals("description", casted.description)
        }

    @Test
    fun `createSession() should return exception when data source call throws client error and payment method intent is CHECKOUT`() =
        runTest {
            val paymentMethodId = "id"
            val paymentMethods =
                listOf(
                    mockk<PaymentMethodConfigDataResponse> {
                        every { type } returns PaymentMethodType.KLARNA.name
                        every { id } returns paymentMethodId
                    },
                )
            val customer =
                mockk<CustomerDataResponse> {
                    every { billingAddress } returns null
                    every { shippingAddress } returns null
                }

            val countryCode = CountryCode.AD
            val currency = "euro"
            val totalOrderAmount = 1
            val lineItemDescription = "description"
            val lineItemUnitAmount = 1
            val lineItemId = "itemId"
            val lineItemQuantity = 1
            val lineItemDiscountAmount = 1
            val lineItemTaxAmount = 1
            val lineItemProductType = "productType"
            val lineItem =
                mockk<OrderDataResponse.LineItemDataResponse> {
                    every { description } returns lineItemDescription
                    every { unitAmount } returns lineItemUnitAmount
                    every { itemId } returns lineItemId
                    every { quantity } returns lineItemQuantity
                    every { discountAmount } returns lineItemDiscountAmount
                    every { taxAmount } returns lineItemTaxAmount
                    every { productType } returns lineItemProductType
                }

            val order =
                mockk<OrderDataResponse> {
                    every { this@mockk.countryCode } returns countryCode
                    every { this@mockk.currencyCode } returns currency
                    every { this@mockk.totalOrderAmount } returns totalOrderAmount
                    every { this@mockk.lineItems } returns listOf(lineItem)
                }

            val coreUrl = "https://www.example.com"
            val configuration =
                mockk<ConfigurationData> {
                    every { this@mockk.coreUrl } returns coreUrl
                    every { this@mockk.paymentMethods } returns paymentMethods
                    every { this@mockk.clientSession.customer } returns customer
                    every { this@mockk.clientSession.order } returns order
                }

            every { configurationDataSource.get() } returns configuration

            val languageTag = "en"
            every { config.settings.locale.toLanguageTag() } returns languageTag

            val exception = Exception()
            coEvery { klarnaCheckoutPaymentSessionDataSource.execute(any()) } throws exception

            val result =
                repository.createSession(surcharge = null, PrimerSessionIntent.CHECKOUT)
                    .exceptionOrNull()

            coVerify {
                klarnaCheckoutPaymentSessionDataSource.execute(
                    BaseRemoteHostRequest(
                        host = coreUrl,
                        data =
                        CreateCheckoutPaymentSessionDataRequest(
                            paymentMethodConfigId = paymentMethodId,
                            sessionType = KlarnaSessionType.ONE_OFF_PAYMENT,
                            totalAmount = totalOrderAmount,
                            localeData =
                            LocaleDataRequest(
                                countryCode = countryCode,
                                currencyCode = currency,
                                localeCode = languageTag,
                            ),
                            orderItems =
                            listOf(
                                CreateCheckoutPaymentSessionDataRequest.OrderItem(
                                    name = lineItemDescription,
                                    unitAmount = lineItemUnitAmount,
                                    reference = lineItemId,
                                    quantity = lineItemQuantity,
                                    discountAmount = lineItemDiscountAmount,
                                    productType = lineItemProductType,
                                    taxAmount = lineItemTaxAmount,
                                ),
                            ),
                            billingAddress = null,
                            shippingAddress = null,
                        ),
                    ),
                )
            }
            assertEquals(exception, result)
        }
}
