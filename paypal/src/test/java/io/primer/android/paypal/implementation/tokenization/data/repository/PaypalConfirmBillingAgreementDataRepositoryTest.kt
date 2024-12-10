package io.primer.android.paypal.implementation.tokenization.data.repository

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.data.network.exception.HttpException
import io.primer.android.errors.data.exception.SessionCreateException
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paypal.implementation.tokenization.data.datasource.RemotePaypalConfirmBillingAgreementDataSource
import io.primer.android.paypal.implementation.tokenization.data.model.PaypalConfirmBillingAgreementDataResponse
import io.primer.android.paypal.implementation.tokenization.data.model.toPaypalConfirmBillingAgreement
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalConfirmBillingAgreementParams
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PaypalConfirmBillingAgreementDataRepositoryTest {

    private lateinit var confirmBillingAgreementDataSource: RemotePaypalConfirmBillingAgreementDataSource
    private lateinit var configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>
    private lateinit var repository: PaypalConfirmBillingAgreementDataRepository

    @BeforeEach
    fun setUp() {
        confirmBillingAgreementDataSource = mockk()
        configurationDataSource = mockk()
        repository =
            PaypalConfirmBillingAgreementDataRepository(confirmBillingAgreementDataSource, configurationDataSource)
    }

    @Test
    fun `confirmBillingAgreement should return PaypalConfirmBillingAgreement on success`() = runTest {
        // Given
        val params = PaypalConfirmBillingAgreementParams("configId", "tokenId")
        val responseData = mockk<PaypalConfirmBillingAgreementDataResponse> {
            every { billingAgreementId } returns "testBillingAgreementId"
            every { externalPayerInfo } returns mockk(relaxed = true)
            every { shippingAddress } returns mockk(relaxed = true)
        }

        coEvery { confirmBillingAgreementDataSource.execute(any()) } returns responseData
        // Mock configuration data retrieval if needed
        every { configurationDataSource.get().coreUrl } returns "https://example.com"

        // When
        val result = repository.confirmBillingAgreement(params)

        // Then
        assertEquals(responseData.toPaypalConfirmBillingAgreement(), result.getOrNull())
    }

    @Test
    fun `confirmBillingAgreement should throw SessionCreateException on HTTP client error`() = runTest {
        // Given
        val testDiagnosticsId = "testDiagnosticsId"
        val testDescription = "testDescription"
        val params = mockk<PaypalConfirmBillingAgreementParams>(relaxed = true)
        val httpException = mockk<HttpException>(relaxed = true) {
            every { isClientError() } returns true
            every { error } returns mockk(relaxed = true) {
                every { diagnosticsId } returns testDiagnosticsId
                every { description } returns testDescription
            }
        }

        coEvery { confirmBillingAgreementDataSource.execute(any()) } throws httpException
        // Mock configuration data retrieval if needed
        every { configurationDataSource.get().coreUrl } returns "https://example.com"

        // When
        val result = requireNotNull(repository.confirmBillingAgreement(params).exceptionOrNull())

        val expected = SessionCreateException(PaymentMethodType.PAYPAL.name, testDiagnosticsId, testDescription)

        // Then
        assert(result is SessionCreateException)
        val unwrappedException = result as SessionCreateException
        assertEquals(expected.description, unwrappedException.description)
        assertEquals(expected.diagnosticsId, unwrappedException.diagnosticsId)
        assertEquals(expected.paymentMethodType, unwrappedException.paymentMethodType)
    }

    @Test
    fun `confirmBillingAgreement should throw SessionCreateException on HTTP server error`() = runTest {
        // Given
        val params = mockk<PaypalConfirmBillingAgreementParams>(relaxed = true)
        val httpException = HttpException(503, mockk(relaxed = true))

        coEvery { confirmBillingAgreementDataSource.execute(any()) } throws httpException
        // Mock configuration data retrieval if needed
        every { configurationDataSource.get().coreUrl } returns "https://example.com"

        // When
        val result = repository.confirmBillingAgreement(params)

        // Then
        assertEquals(httpException, result.exceptionOrNull())
    }
}
