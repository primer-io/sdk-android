package io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.datasource.RemoteFinalizeKlarnaSessionDataSource
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.FinalizeKlarnaSessionDataRequest
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.FinalizeKlarnaSessionDataResponse
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaCustomerTokenParam
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.models.ConfigurationData
import io.primer.android.data.configuration.models.PaymentMethodConfigDataResponse
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.exception.SessionCreateException
import io.primer.android.http.exception.HttpException
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
    private lateinit var localConfigurationDataSource: LocalConfigurationDataSource

    @InjectMockKs
    private lateinit var repository: FinalizeKlarnaSessionDataRepository

    @Test
    fun `finalize() should return response when data source call succeeds`() = runTest {
        val sessionId = "sessionId"
        val params = mockk<KlarnaCustomerTokenParam> {
            every { this@mockk.sessionId } returns sessionId
        }
        val paymentMethodId = "id"
        val paymentMethods = listOf(
            mockk<PaymentMethodConfigDataResponse> {
                every { type } returns PaymentMethodType.KLARNA.name
                every { id } returns paymentMethodId
            }
        )
        val configuration = mockk<ConfigurationData> {
            every { this@mockk.paymentMethods } returns paymentMethods
        }
        every { localConfigurationDataSource.getConfiguration() } returns configuration
        val response = mockk<FinalizeKlarnaSessionDataResponse>()
        coEvery { remoteFinalizeKlarnaSessionDataSource.execute(any()) } returns response

        val result = repository.finalize(params).getOrNull()

        coVerify {
            remoteFinalizeKlarnaSessionDataSource.execute(
                BaseRemoteRequest(
                    configuration = configuration,
                    data = FinalizeKlarnaSessionDataRequest(
                        paymentMethodConfigId = paymentMethodId,
                        sessionId = sessionId
                    )
                )
            )
        }
        assertEquals(response, result)
    }

    @Test
    fun `finalize() should return SessionCreateException when data source call throws client error HttpException`() = runTest {
        val sessionId = "sessionId"
        val params = mockk<KlarnaCustomerTokenParam> {
            every { this@mockk.sessionId } returns sessionId
        }
        val paymentMethodId = "id"
        val paymentMethods = listOf(
            mockk<PaymentMethodConfigDataResponse> {
                every { type } returns PaymentMethodType.KLARNA.name
                every { id } returns paymentMethodId
            }
        )
        val configuration = mockk<ConfigurationData> {
            every { this@mockk.paymentMethods } returns paymentMethods
        }
        every { localConfigurationDataSource.getConfiguration() } returns configuration
        val exception = mockk<HttpException> {
            every { isClientError() } returns true
            every { error.diagnosticsId } returns "diagnosticId"
            every { error.description } returns "description"
        }
        coEvery { remoteFinalizeKlarnaSessionDataSource.execute(any()) } throws exception

        val result = repository.finalize(params).exceptionOrNull()

        coVerify {
            remoteFinalizeKlarnaSessionDataSource.execute(
                BaseRemoteRequest(
                    configuration = configuration,
                    data = FinalizeKlarnaSessionDataRequest(
                        paymentMethodConfigId = paymentMethodId,
                        sessionId = sessionId
                    )
                )
            )
        }
        assertInstanceOf(SessionCreateException::class.java, result)
        val casted = result as SessionCreateException
        assertEquals(PaymentMethodType.KLARNA, casted.paymentMethodType)
        assertEquals("diagnosticId", casted.diagnosticsId)
        assertEquals("description", casted.description)
    }

    @Test
    fun `finalize() should return exception when data source call throws client error`() = runTest {
        val sessionId = "sessionId"
        val params = mockk<KlarnaCustomerTokenParam> {
            every { this@mockk.sessionId } returns sessionId
        }
        val paymentMethodId = "id"
        val paymentMethods = listOf(
            mockk<PaymentMethodConfigDataResponse> {
                every { type } returns PaymentMethodType.KLARNA.name
                every { id } returns paymentMethodId
            }
        )
        val configuration = mockk<ConfigurationData> {
            every { this@mockk.paymentMethods } returns paymentMethods
        }
        every { localConfigurationDataSource.getConfiguration() } returns configuration
        val exception = Exception()
        coEvery { remoteFinalizeKlarnaSessionDataSource.execute(any()) } throws exception

        val result = repository.finalize(params).exceptionOrNull()

        coVerify {
            remoteFinalizeKlarnaSessionDataSource.execute(
                BaseRemoteRequest(
                    configuration = configuration,
                    data = FinalizeKlarnaSessionDataRequest(
                        paymentMethodConfigId = paymentMethodId,
                        sessionId = sessionId
                    )
                )
            )
        }
        assertEquals(exception, result)
    }
}
