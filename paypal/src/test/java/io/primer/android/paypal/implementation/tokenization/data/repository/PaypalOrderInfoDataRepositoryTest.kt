package io.primer.android.paypal.implementation.tokenization.data.repository

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.paypal.implementation.tokenization.data.datasource.RemotePaypalOrderInfoDataSource
import io.primer.android.paypal.implementation.tokenization.data.model.PaypalOrderInfoResponse
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalOrderInfo
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalOrderInfoParams
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PaypalOrderInfoDataRepositoryTest {

    private lateinit var remotePaypalOrderInfoDataSource: RemotePaypalOrderInfoDataSource
    private lateinit var configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>
    private lateinit var repository: PaypalOrderInfoDataRepository

    // Reusable test data
    private val testOrderId = "testOrderId"
    private val testEmail = "test@email.com"
    private val testExternalPayerId = "externalPayerId"
    private val testFirstName = "testFirstName"
    private val testLastName = "testLastName"
    private val testCoreUrl = "https://example.com"

    @BeforeEach
    fun setUp() {
        remotePaypalOrderInfoDataSource = mockk()
        configurationDataSource = mockk()
        repository = PaypalOrderInfoDataRepository(remotePaypalOrderInfoDataSource, configurationDataSource)
    }

    @Test
    fun `getPaypalOrderInfo should return PaypalOrderInfo on success`() = runTest {
        // Given
        val params = PaypalOrderInfoParams("configId", "orderId")
        val responseData = mockk<PaypalOrderInfoResponse>() {
            every { orderId } returns testOrderId
            every { externalPayerInfo } returns mockk(relaxed = true) {
                every { email } returns testEmail
                every { externalPayerId } returns testExternalPayerId
                every { firstName } returns testFirstName
                every { lastName } returns testLastName
            }
        }

        every { configurationDataSource.get() } returns mockk(relaxed = true) {
            every { coreUrl } returns testCoreUrl
        }

        coEvery { remotePaypalOrderInfoDataSource.execute(any()) } returns responseData

        // When
        val result = repository.getPaypalOrderInfo(params)

        val expectedResult = PaypalOrderInfo(
            orderId = testOrderId,
            email = testEmail,
            externalPayerId = testExternalPayerId,
            externalPayerFirstName = testFirstName,
            externalPayerLastName = testLastName
        )

        // Then
        assertEquals(expectedResult, result.getOrNull())
    }
}
