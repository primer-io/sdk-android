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
import io.primer.android.configuration.data.model.PaymentMethodConfigDataResponse
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.exception.HttpException
import io.primer.android.errors.data.exception.SessionCreateException
import io.primer.android.klarna.implementation.session.data.datasource.RemoteFinalizeKlarnaSessionDataSource
import io.primer.android.klarna.implementation.session.data.models.FinalizeKlarnaSessionDataRequest
import io.primer.android.klarna.implementation.session.data.models.FinalizeKlarnaSessionDataResponse
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
class FinalizeKlarnaSessionDataRepositoryTest {
    @MockK
    private lateinit var remoteFinalizeKlarnaSessionDataSource:
        RemoteFinalizeKlarnaSessionDataSource

    @MockK
    private lateinit var configurationDataSource: CacheConfigurationDataSource

    @InjectMockKs
    private lateinit var repository: FinalizeKlarnaSessionDataRepository

    @Test
    fun `finalize() should return response when data source call succeeds`() =
        runTest {
            val sessionId = "sessionId"
            val params =
                mockk<KlarnaCustomerTokenParam> {
                    every { this@mockk.sessionId } returns sessionId
                }
            val paymentMethodId = "id"
            val paymentMethods =
                listOf(
                    mockk<PaymentMethodConfigDataResponse> {
                        every { type } returns PaymentMethodType.KLARNA.name
                        every { id } returns paymentMethodId
                    },
                )

            val coreUrl = "https://www.example.com"
            val configuration =
                mockk<ConfigurationData> {
                    every { this@mockk.coreUrl } returns coreUrl
                    every { this@mockk.paymentMethods } returns paymentMethods
                }
            every { configurationDataSource.get() } returns configuration

            val response = mockk<FinalizeKlarnaSessionDataResponse>()
            coEvery { remoteFinalizeKlarnaSessionDataSource.execute(any()) } returns response

            val result = repository.finalize(params).getOrThrow()

            coVerify {
                remoteFinalizeKlarnaSessionDataSource.execute(
                    BaseRemoteHostRequest(
                        host = coreUrl,
                        data =
                            FinalizeKlarnaSessionDataRequest(
                                paymentMethodConfigId = paymentMethodId,
                                sessionId = sessionId,
                            ),
                    ),
                )
            }
            assertEquals(response, result)
        }

    @Test
    fun `finalize() should return SessionCreateException when data source call throws client error HttpException`() =
        runTest {
            val sessionId = "sessionId"
            val params =
                mockk<KlarnaCustomerTokenParam> {
                    every { this@mockk.sessionId } returns sessionId
                }
            val paymentMethodId = "id"
            val paymentMethods =
                listOf(
                    mockk<PaymentMethodConfigDataResponse> {
                        every { type } returns PaymentMethodType.KLARNA.name
                        every { id } returns paymentMethodId
                    },
                )
            val coreUrl = "https://www.example.com"
            val configuration =
                mockk<ConfigurationData> {
                    every { this@mockk.coreUrl } returns coreUrl
                    every { this@mockk.paymentMethods } returns paymentMethods
                }
            every { configurationDataSource.get() } returns configuration

            val exception =
                mockk<HttpException> {
                    every { isClientError() } returns true
                    every { error.diagnosticsId } returns "diagnosticId"
                    every { error.description } returns "description"
                }
            coEvery { remoteFinalizeKlarnaSessionDataSource.execute(any()) } throws exception

            val result = repository.finalize(params).exceptionOrNull()

            coVerify {
                remoteFinalizeKlarnaSessionDataSource.execute(
                    BaseRemoteHostRequest(
                        host = coreUrl,
                        data =
                            FinalizeKlarnaSessionDataRequest(
                                paymentMethodConfigId = paymentMethodId,
                                sessionId = sessionId,
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
    fun `finalize() should return exception when data source call throws client error`() =
        runTest {
            val sessionId = "sessionId"
            val params =
                mockk<KlarnaCustomerTokenParam> {
                    every { this@mockk.sessionId } returns sessionId
                }
            val paymentMethodId = "id"
            val paymentMethods =
                listOf(
                    mockk<PaymentMethodConfigDataResponse> {
                        every { type } returns PaymentMethodType.KLARNA.name
                        every { id } returns paymentMethodId
                    },
                )
            val coreUrl = "https://www.example.com"
            val configuration =
                mockk<ConfigurationData> {
                    every { this@mockk.coreUrl } returns coreUrl
                    every { this@mockk.paymentMethods } returns paymentMethods
                }
            every { configurationDataSource.get() } returns configuration

            val exception = Exception()
            coEvery { remoteFinalizeKlarnaSessionDataSource.execute(any()) } throws exception

            val result = repository.finalize(params).exceptionOrNull()

            coVerify {
                remoteFinalizeKlarnaSessionDataSource.execute(
                    BaseRemoteHostRequest(
                        host = coreUrl,
                        data =
                            FinalizeKlarnaSessionDataRequest(
                                paymentMethodConfigId = paymentMethodId,
                                sessionId = sessionId,
                            ),
                    ),
                )
            }
            assertEquals(exception, result)
        }
}
