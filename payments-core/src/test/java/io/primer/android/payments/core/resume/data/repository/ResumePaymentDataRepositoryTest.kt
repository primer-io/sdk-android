package io.primer.android.payments.core.resume.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.exception.HttpException
import io.primer.android.payments.core.create.data.model.PaymentDataResponse
import io.primer.android.payments.core.errors.data.exception.PaymentResumeException
import io.primer.android.payments.core.resume.data.datasource.ResumePaymentDataSource
import io.primer.android.payments.core.resume.data.model.ResumePaymentDataRequest
import io.primer.android.payments.core.resume.domain.respository.ResumePaymentsRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ResumePaymentDataRepositoryTest {

    private lateinit var resumePaymentDataSource: ResumePaymentDataSource
    private lateinit var configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>
    private lateinit var repository: ResumePaymentsRepository

    @BeforeEach
    fun setup() {
        resumePaymentDataSource = mockk()
        configurationDataSource = mockk()
        repository = ResumePaymentDataRepository(resumePaymentDataSource, configurationDataSource)
    }

    @Test
    fun `resumePayment should call resumePaymentDataSource_execute on successful response`() = runTest {
        // Given
        val paymentId = "paymentId"
        val resumeToken = "resumeToken"
        val configurationData = mockk<ConfigurationData> {
            every { pciUrl } returns "https://pci.url"
        }
        val remoteRequest = BaseRemoteHostRequest(
            host = configurationData.pciUrl,
            data = Pair(paymentId, ResumePaymentDataRequest(resumeToken))
        )
        val expectedResult = mockk<PaymentDataResponse>(relaxed = true)

        coEvery { configurationDataSource.get() } returns configurationData
        coEvery { resumePaymentDataSource.execute(remoteRequest) } returns expectedResult

        // When
        val result = repository.resumePayment(paymentId, resumeToken)
        assertTrue(result.isSuccess)

        // Then
        coVerify { configurationDataSource.get() }
        coVerify { resumePaymentDataSource.execute(remoteRequest) }
        // Add more assertions here to check the result if necessary
    }

    @Test
    fun `resumePayment throws PaymentResumeException on client error`() = runTest {
        // Given
        val paymentId = "paymentId"
        val resumeToken = "resumeToken"
        val configurationData = mockk<ConfigurationData> {
            every { pciUrl } returns "https://pci.url"
        }
        val remoteRequest = BaseRemoteHostRequest(
            host = configurationData.pciUrl,
            data = Pair(paymentId, ResumePaymentDataRequest(resumeToken))
        )
        val httpException = mockk<HttpException> {
            every { isClientError() } returns true
        }

        coEvery { configurationDataSource.get() } returns configurationData
        coEvery { resumePaymentDataSource.execute(remoteRequest) } throws httpException

        // When / Then

        val result = repository.resumePayment(paymentId, resumeToken)
        assertTrue(result.isFailure)
        assertThrows(PaymentResumeException::class.java) { result.getOrThrow() }

        coVerify { configurationDataSource.get() }
        coVerify { resumePaymentDataSource.execute(remoteRequest) }
    }

    @Test
    fun `resumePayment rethrows non-client error exceptions`() = runTest {
        // Given
        val paymentId = "paymentId"
        val resumeToken = "resumeToken"
        val configurationData = mockk<ConfigurationData> {
            every { pciUrl } returns "https://pci.url"
        }
        val remoteRequest = BaseRemoteHostRequest(
            host = configurationData.pciUrl,
            data = Pair(paymentId, ResumePaymentDataRequest(resumeToken))
        )
        val genericException = RuntimeException("Some other error")

        coEvery { configurationDataSource.get() } returns configurationData
        coEvery { resumePaymentDataSource.execute(remoteRequest) } throws genericException

        // When / Then
        val result = repository.resumePayment(paymentId, resumeToken)
        assertTrue(result.isFailure)
        assertThrows(genericException::class.java) { result.getOrThrow() }

        coVerify { configurationDataSource.get() }
        coVerify { resumePaymentDataSource.execute(remoteRequest) }
    }
}
