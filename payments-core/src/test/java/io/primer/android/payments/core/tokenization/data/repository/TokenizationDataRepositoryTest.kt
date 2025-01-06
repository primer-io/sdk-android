package io.primer.android.payments.core.tokenization.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.payments.core.tokenization.data.datasource.BaseRemoteTokenizationDataSource
import io.primer.android.payments.core.tokenization.data.mapper.TokenizationParamsMapper
import io.primer.android.payments.core.tokenization.data.model.BasePaymentInstrumentDataRequest
import io.primer.android.payments.core.tokenization.data.model.TokenizationRequestV2
import io.primer.android.payments.core.tokenization.domain.model.TokenizationParams
import io.primer.android.payments.core.tokenization.domain.model.paymentInstruments.BasePaymentInstrumentParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class TokenizationDataRepositoryTest {
    // Mock dependencies
    private val mockRemoteDataSource = mockk<BaseRemoteTokenizationDataSource<BasePaymentInstrumentDataRequest>>()
    private val mockCacheDataSource = mockk<BaseCacheDataSource<ConfigurationData, ConfigurationData>>()
    private val mockTokenizationParamsMapper =
        mockk<TokenizationParamsMapper<BasePaymentInstrumentParams, BasePaymentInstrumentDataRequest>>()

    // Create an instance of the repository with mocked dependencies
    private val repository =
        object : TokenizationDataRepository<BasePaymentInstrumentParams, BasePaymentInstrumentDataRequest>(
            mockRemoteDataSource,
            mockCacheDataSource,
            mockTokenizationParamsMapper,
        ) {}

    @Test
    fun `tokenize() calls remoteDataSource_execute with correct parameters when success`() =
        runTest {
            // Given
            val tokenizationParams = mockk<TokenizationParams<BasePaymentInstrumentParams>>()
            val configurationData =
                mockk<ConfigurationData> {
                    every { pciUrl } returns "https://example.com"
                }
            val tokenizationResult = mockk<TokenizationRequestV2<BasePaymentInstrumentDataRequest>>(relaxed = true)

            every { mockCacheDataSource.get() } returns configurationData
            every { mockTokenizationParamsMapper.map(tokenizationParams) } returns tokenizationResult

            coEvery { mockRemoteDataSource.execute(any()) } returns mockk()

            // When
            repository.tokenize(tokenizationParams)

//        // Verify interactions
            verify { mockCacheDataSource.get() }
            verify { mockTokenizationParamsMapper.map(tokenizationParams) }
            coVerify { mockRemoteDataSource.execute(any()) }
        }
}
