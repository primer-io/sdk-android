package io.primer.android.paypal.implementation.tokenization.data.repository

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.data.network.exception.HttpException
import io.primer.android.errors.data.exception.SessionCreateException
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paypal.implementation.tokenization.data.datasource.RemotePaypalCreateBillingAgreementDataSource
import io.primer.android.paypal.implementation.tokenization.data.model.PaypalCreateBillingAgreementDataResponse
import io.primer.android.paypal.implementation.tokenization.data.model.toBillingAgreement
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalCreateBillingAgreementParams
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PaypalCreateBillingAgreementDataRepositoryTest {

    private lateinit var createBillingAgreementDataSource: RemotePaypalCreateBillingAgreementDataSource
    private lateinit var configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>
    private lateinit var repository: PaypalCreateBillingAgreementDataRepository

    @BeforeEach
    fun setUp() {
        createBillingAgreementDataSource = mockk()
        configurationDataSource = mockk()
        repository =
            PaypalCreateBillingAgreementDataRepository(createBillingAgreementDataSource, configurationDataSource)
    }

    @Test
    fun `createBillingAgreement should return PaypalCreateBillingAgreement on success`() = runTest {
        // Given
        val paymentMethodConfigId = "configId"
        val successUrl = "successUrl"
        val cancelUrl = "cancelUrl"

        val params = PaypalCreateBillingAgreementParams(paymentMethodConfigId, successUrl, cancelUrl)
        val responseData = mockk<PaypalCreateBillingAgreementDataResponse>(relaxed = true)

        coEvery { createBillingAgreementDataSource.execute(any()) } returns responseData
        // Mock configuration data retrieval if needed
        every { configurationDataSource.get().coreUrl } returns "https://example.com"

        // When
        val result = repository.createBillingAgreement(params)

        // Then
        assertEquals(responseData.toBillingAgreement(paymentMethodConfigId, successUrl, cancelUrl), result.getOrNull())
    }

    @Test
    fun `createBillingAgreement should throw SessionCreateException on HTTP client error`() = runTest {
        // Given
        val testDiagnosticsId = "testDiagnosticsId"
        val testDescription = "testDescription"
        val params = mockk<PaypalCreateBillingAgreementParams>(relaxed = true)

        val httpException = mockk<HttpException>(relaxed = true) {
            every { isClientError() } returns true
            every { error } returns mockk(relaxed = true) {
                every { diagnosticsId } returns testDiagnosticsId
                every { description } returns testDescription
            }
        }

        coEvery { createBillingAgreementDataSource.execute(any()) } throws httpException
        // Mock configuration data retrieval if needed
        every { configurationDataSource.get().coreUrl } returns "https://example.com"

        // When
        val result = requireNotNull(repository.createBillingAgreement(params).exceptionOrNull())

        val expected = SessionCreateException(PaymentMethodType.PAYPAL.name, testDiagnosticsId, testDescription)

        // Then
        assert(result is SessionCreateException)
        val unwrappedException = result as SessionCreateException
        assertEquals(expected.description, unwrappedException.description)
        assertEquals(expected.diagnosticsId, unwrappedException.diagnosticsId)
        assertEquals(expected.paymentMethodType, unwrappedException.paymentMethodType)
    }

    @Test
    fun `createBillingAgreement should throw SessionCreateException on HTTP server error`() = runTest {
        // Given
        val params = mockk<PaypalCreateBillingAgreementParams>(relaxed = true)
        val httpException = HttpException(503, mockk(relaxed = true))

        coEvery { createBillingAgreementDataSource.execute(any()) } throws httpException
        // Mock configuration data retrieval if needed
        every { configurationDataSource.get().coreUrl } returns "https://example.com"

        // When
        val result = repository.createBillingAgreement(params)

        // Then
        assertEquals(httpException, result.exceptionOrNull())
    }
}
