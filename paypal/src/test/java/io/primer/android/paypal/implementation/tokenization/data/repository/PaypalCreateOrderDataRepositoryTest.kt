package io.primer.android.paypal.implementation.tokenization.data.repository

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.paypal.implementation.tokenization.data.datasource.RemotePaypalCreateOrderDataSource
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalCreateOrderParams
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalOrder
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PaypalCreateOrderDataRepositoryTest {

    private lateinit var createOrderDataSource: RemotePaypalCreateOrderDataSource
    private lateinit var configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>
    private lateinit var repository: PaypalCreateOrderDataRepository

    // Reusable test data
    private val testPaymentMethodConfigId = "testPaymentMethodConfigId"
    private val testAmount = 1000
    private val testCurrencyCode = "USD"
    private val testSuccessUrl = "https://example.com/success"
    private val testCancelUrl = "https://example.com/cancel"
    private val testCoreUrl = "https://example.com"
    private val testOrderId = "orderId"
    private val testApprovalUrl = "approvalUrl"

    @BeforeEach
    fun setUp() {
        createOrderDataSource = mockk()
        configurationDataSource = mockk()
        repository = PaypalCreateOrderDataRepository(createOrderDataSource, configurationDataSource)
    }

    @Test
    fun `createOrder should return PaypalOrder on success`() = runTest {
        // Given
        val params = PaypalCreateOrderParams(
            paymentMethodConfigId = testPaymentMethodConfigId,
            amount = testAmount,
            currencyCode = testCurrencyCode,
            successUrl = testSuccessUrl,
            cancelUrl = testCancelUrl
        )

        every { configurationDataSource.get() } returns mockk(relaxed = true) {
            every { coreUrl } returns testCoreUrl
        }

        coEvery { createOrderDataSource.execute(any()) } returns mockk(relaxed = true) {
            every { orderId } returns testOrderId
            every { approvalUrl } returns testApprovalUrl
        }

        // When
        val result = repository.createOrder(params)

        val expectedOrder = PaypalOrder(
            orderId = testOrderId,
            approvalUrl = testApprovalUrl,
            successUrl = testSuccessUrl,
            cancelUrl = testCancelUrl
        )

        // Then
        assertEquals(expectedOrder, result.getOrNull())
    }
}
