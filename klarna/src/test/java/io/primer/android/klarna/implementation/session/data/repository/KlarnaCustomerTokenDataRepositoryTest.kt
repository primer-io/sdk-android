package io.primer.android.klarna.implementation.session.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.configuration.data.model.CountryCode
import io.primer.android.configuration.data.model.OrderDataResponse
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.exception.HttpException
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.errors.data.exception.SessionCreateException
import io.primer.android.klarna.implementation.session.data.datasource.RemoteKlarnaCustomerTokenDataSource
import io.primer.android.klarna.implementation.session.data.models.CreateCustomerTokenDataRequest
import io.primer.android.klarna.implementation.session.data.models.CreateCustomerTokenDataResponse
import io.primer.android.klarna.implementation.session.data.models.LocaleDataRequest
import io.primer.android.klarna.implementation.session.domain.models.KlarnaCustomerTokenParam
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class KlarnaCustomerTokenDataRepositoryTest {
    @MockK
    private lateinit var remoteKlarnaCustomerTokenDataSource: RemoteKlarnaCustomerTokenDataSource

    @MockK
    private lateinit var configurationDataSource: CacheConfigurationDataSource

    @MockK
    private lateinit var config: PrimerConfig

    @InjectMockKs
    private lateinit var repository: KlarnaCustomerTokenDataRepository

    @Test
    fun `createCustomerToken() should return response when data source call succeeds`() =
        runTest {
            val sessionId = "sessionId"
            val authorizationToken = "authorizationToken"
            val params =
                mockk<KlarnaCustomerTokenParam> {
                    every { this@mockk.sessionId } returns sessionId
                    every { this@mockk.authorizationToken } returns authorizationToken
                }
            val paymentMethodId = "id"
            val paymentMethods =
                listOf(
                    mockk<PaymentMethodConfigDataResponse> {
                        every { type } returns PaymentMethodType.KLARNA.name
                        every { id } returns paymentMethodId
                    },
                )
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
                    every { this@mockk.clientSession.order } returns order
                }
            every { configurationDataSource.get() } returns configuration

            val recurringPaymentDescription = "recurringPaymentDescription"
            every {
                config.settings.paymentMethodOptions.klarnaOptions.recurringPaymentDescription
            } returns recurringPaymentDescription
            val languageTag = "en"
            every { config.settings.locale.toLanguageTag() } returns languageTag
            val response = mockk<CreateCustomerTokenDataResponse>()
            coEvery { remoteKlarnaCustomerTokenDataSource.execute(any()) } returns response

            val result = repository.createCustomerToken(params).getOrNull()

            coVerify {
                remoteKlarnaCustomerTokenDataSource.execute(
                    BaseRemoteHostRequest(
                        host = coreUrl,
                        data =
                        CreateCustomerTokenDataRequest(
                            paymentMethodConfigId = paymentMethodId,
                            sessionId = sessionId,
                            authorizationToken = authorizationToken,
                            description = recurringPaymentDescription,
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
            assertEquals(response, result)
        }

    @Test
    fun `createCustomerToken() should return SessionCreateException when data source call throws client error HttpException`() =
        runTest {
            val sessionId = "sessionId"
            val authorizationToken = "authorizationToken"
            val params =
                mockk<KlarnaCustomerTokenParam> {
                    every { this@mockk.sessionId } returns sessionId
                    every { this@mockk.authorizationToken } returns authorizationToken
                }
            val paymentMethodId = "id"
            val paymentMethods =
                listOf(
                    mockk<PaymentMethodConfigDataResponse> {
                        every { type } returns PaymentMethodType.KLARNA.name
                        every { id } returns paymentMethodId
                    },
                )

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
                    every { this@mockk.clientSession.order } returns order
                }
            every { configurationDataSource.get() } returns configuration

            val recurringPaymentDescription = "recurringPaymentDescription"
            every {
                config.settings.paymentMethodOptions.klarnaOptions.recurringPaymentDescription
            } returns recurringPaymentDescription
            val languageTag = "en"
            every { config.settings.locale.toLanguageTag() } returns languageTag
            val exception =
                mockk<HttpException> {
                    every { isClientError() } returns true
                    every { error.diagnosticsId } returns "diagnosticId"
                    every { error.description } returns "description"
                }
            coEvery { remoteKlarnaCustomerTokenDataSource.execute(any()) } throws exception

            val result = repository.createCustomerToken(params).exceptionOrNull()

            coVerify {
                remoteKlarnaCustomerTokenDataSource.execute(
                    BaseRemoteHostRequest(
                        host = coreUrl,
                        data =
                        CreateCustomerTokenDataRequest(
                            paymentMethodConfigId = paymentMethodId,
                            sessionId = sessionId,
                            authorizationToken = authorizationToken,
                            description = recurringPaymentDescription,
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
    fun `createCustomerToken() should return exception when data source call throws client error`() =
        runTest {
            val sessionId = "sessionId"
            val authorizationToken = "authorizationToken"
            val params =
                mockk<KlarnaCustomerTokenParam> {
                    every { this@mockk.sessionId } returns sessionId
                    every { this@mockk.authorizationToken } returns authorizationToken
                }
            val paymentMethodId = "id"
            val paymentMethods =
                listOf(
                    mockk<PaymentMethodConfigDataResponse> {
                        every { type } returns PaymentMethodType.KLARNA.name
                        every { id } returns paymentMethodId
                    },
                )

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
                    every { this@mockk.clientSession.order } returns order
                }
            every { configurationDataSource.get() } returns configuration

            val recurringPaymentDescription = "recurringPaymentDescription"
            every {
                config.settings.paymentMethodOptions.klarnaOptions.recurringPaymentDescription
            } returns recurringPaymentDescription
            val languageTag = "en"
            every { config.settings.locale.toLanguageTag() } returns languageTag
            val exception = Exception()
            coEvery { remoteKlarnaCustomerTokenDataSource.execute(any()) } throws exception

            val result = repository.createCustomerToken(params).exceptionOrNull()

            coVerify {
                remoteKlarnaCustomerTokenDataSource.execute(
                    BaseRemoteHostRequest(
                        host = coreUrl,
                        data =
                        CreateCustomerTokenDataRequest(
                            paymentMethodConfigId = paymentMethodId,
                            sessionId = sessionId,
                            authorizationToken = authorizationToken,
                            description = recurringPaymentDescription,
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
}
