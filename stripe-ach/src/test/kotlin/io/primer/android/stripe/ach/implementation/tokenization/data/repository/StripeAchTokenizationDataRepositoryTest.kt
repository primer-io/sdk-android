package io.primer.android.stripe.ach.implementation.tokenization.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.payments.core.tokenization.data.mapper.TokenizationParamsMapper
import io.primer.android.payments.core.tokenization.data.model.PaymentMethodTokenInternal
import io.primer.android.payments.core.tokenization.data.model.TokenizationRequestV2
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.stripe.ach.implementation.tokenization.data.model.StripeAchPaymentInstrumentDataRequest
import io.primer.android.stripe.ach.implementation.tokenization.domain.model.StripeAchPaymentInstrumentParams
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class StripeAchTokenizationDataRepositoryTest {
    @MockK
    private lateinit var remoteTokenizationDataSource:
        BaseRemoteTokenizationDataSource<StripeAchPaymentInstrumentDataRequest>

    @MockK
    private lateinit var localConfigurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>

    @MockK
    private lateinit var tokenizationParamsMapper:
        TokenizationParamsMapper<StripeAchPaymentInstrumentParams, StripeAchPaymentInstrumentDataRequest>

    @InjectMockKs
    private lateinit var repository: StripeAchTokenizationDataRepository

    @Test
    fun `tokenize() returns success result when data source executes successfully`() =
        runBlocking {
            val params = mockk<TokenizationParams<StripeAchPaymentInstrumentParams>>()
            val expectedRequest = mockk<TokenizationRequestV2<StripeAchPaymentInstrumentDataRequest>>()
            val expectedResponse = mockk<PaymentMethodTokenInternal>()
            val configurationData =
                mockk<ConfigurationData> {
                    every { pciUrl } returns "pciUrl"
                }

            every { localConfigurationDataSource.get() } returns configurationData
            every { tokenizationParamsMapper.map(params) } returns expectedRequest
            coEvery { remoteTokenizationDataSource.execute(any()) } returns expectedResponse

            val response = repository.tokenize(params)

            assertEquals(Result.success(expectedResponse), response)
            verify {
                localConfigurationDataSource.get()
                tokenizationParamsMapper.map(params)
                configurationData.pciUrl
            }
            coVerify {
                remoteTokenizationDataSource.execute(
                    BaseRemoteHostRequest(
                        host = "pciUrl",
                        data = expectedRequest,
                    ),
                )
            }
        }

    @Test
    fun `tokenize() returns failure result when data source execution fails`() =
        runBlocking {
            val params = mockk<TokenizationParams<StripeAchPaymentInstrumentParams>>()
            val expectedRequest = mockk<TokenizationRequestV2<StripeAchPaymentInstrumentDataRequest>>()
            val configurationData =
                mockk<ConfigurationData> {
                    every { pciUrl } returns "pciUrl"
                }
            val exception = Exception("Execution failed")

            every { localConfigurationDataSource.get() } returns configurationData
            every { tokenizationParamsMapper.map(params) } returns expectedRequest
            coEvery { remoteTokenizationDataSource.execute(any()) } throws exception

            val response = repository.tokenize(params)

            assertEquals(Result.failure<Any>(exception), response)
            verify {
                localConfigurationDataSource.get()
                tokenizationParamsMapper.map(params)
            }
            coVerify {
                remoteTokenizationDataSource.execute(
                    BaseRemoteHostRequest(
                        host = "pciUrl",
                        data = expectedRequest,
                    ),
                )
            }
        }
}
