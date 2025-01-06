package io.primer.android.payments.core.create.data.repository

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.data.network.exception.HttpException
import io.primer.android.payments.core.create.data.datasource.CreatePaymentDataSource
import io.primer.android.payments.core.create.data.datasource.LocalPaymentDataSource
import io.primer.android.payments.core.create.data.model.PaymentDataResponse
import io.primer.android.payments.core.create.data.model.PaymentStatus
import io.primer.android.payments.core.create.domain.repository.CreatePaymentRepository
import io.primer.android.payments.core.errors.data.exception.PaymentCreateException
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CreatePaymentDataRepositoryTest {
    private lateinit var createPaymentDataSource: CreatePaymentDataSource
    private lateinit var localPaymentDataSource: LocalPaymentDataSource
    private lateinit var configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>
    private lateinit var repository: CreatePaymentRepository

    @BeforeEach
    fun setUp() {
        createPaymentDataSource = mockk()
        localPaymentDataSource = mockk()
        configurationDataSource = mockk()
        repository =
            CreatePaymentDataRepository(createPaymentDataSource, localPaymentDataSource, configurationDataSource)
    }

    @Test
    fun `createPayment should return PaymentResult when successful`() =
        runTest {
            // Arrange
            val token = "testToken"
            val configurationData =
                mockk<ConfigurationData> {
                    every { pciUrl } returns "https://test.com"
                }
            val paymentResponse =
                PaymentDataResponse(
                    id = "payment123",
                    date = "2024-06-25",
                    status = PaymentStatus.SUCCESS,
                    orderId = "order456",
                    currencyCode = "USD",
                    amount = 1000,
                    customerId = "customer789",
                    paymentFailureReason = null,
                    requiredAction = null,
                    showSuccessCheckoutOnPendingPayment = false,
                )

            coEvery { configurationDataSource.get() } returns configurationData
            coEvery { createPaymentDataSource.execute(any()) } returns paymentResponse
            coEvery { localPaymentDataSource.update(paymentResponse) } just Runs

            // Act
            val paymentResult = repository.createPayment(token)

            // Assert
            assertNotNull(paymentResult.isSuccess)
            val result = paymentResult.getOrNull()!!

            assertEquals("payment123", result.payment.id)
            assertEquals("order456", result.payment.orderId)
            assertEquals(PaymentStatus.SUCCESS, result.paymentStatus)
            coVerify { configurationDataSource.get() }
            coVerify { createPaymentDataSource.execute(any()) }
            coVerify { localPaymentDataSource.update(paymentResponse) }
        }

    @Test
    fun `createPayment should throw PaymentCreateException when HttpException occurs with client error`() =
        runTest {
            // Arrange
            val token = "testToken"
            val configurationData =
                mockk<ConfigurationData> {
                    every { pciUrl } returns "https://test.com"
                }
            val httpException =
                mockk<HttpException> {
                    every { isClientError() } returns true
                }

            coEvery { configurationDataSource.get() } returns configurationData
            coEvery { createPaymentDataSource.execute(any()) } throws httpException

            // Act & Assert
            val result = repository.createPayment(token)
            assertTrue(result.isFailure)

            assertTrue(result.exceptionOrNull() is PaymentCreateException)
            coVerify { configurationDataSource.get() }
            coVerify { createPaymentDataSource.execute(any()) }
        }

    @Test
    fun `createPayment should rethrow exception when non-client error HttpException occurs`() =
        runTest {
            // Arrange
            val token = "testToken"
            val configurationData =
                mockk<ConfigurationData> {
                    every { pciUrl } returns "https://test.com"
                }
            val httpException =
                mockk<HttpException> {
                    every { isClientError() } returns false
                    every { isPaymentError() } returns false
                }

            coEvery { configurationDataSource.get() } returns configurationData
            coEvery { createPaymentDataSource.execute(any()) } throws httpException

            // Act & Assert
            val result = repository.createPayment(token)
            assertTrue(result.isFailure)

            assertTrue(result.exceptionOrNull() is HttpException)
            coVerify { configurationDataSource.get() }
            coVerify { createPaymentDataSource.execute(any()) }
        }

    @Test
    fun `createPayment should rethrow exception when non-HttpException occurs`() =
        runTest {
            // Arrange
            val token = "testToken"
            val configurationData =
                mockk<ConfigurationData> {
                    every { pciUrl } returns "https://test.com"
                }
            val exception = RuntimeException("Something went wrong")

            coEvery { configurationDataSource.get() } returns configurationData
            coEvery { createPaymentDataSource.execute(any()) } throws exception

            // Act & Assert
            val result = repository.createPayment(token)
            assertTrue(result.isFailure)

            assertEquals(exception, result.exceptionOrNull())
            coVerify { configurationDataSource.get() }
            coVerify { createPaymentDataSource.execute(any()) }
        }
}
