package io.primer.android.payments.core.create.data.repository

import io.mockk.every
import io.mockk.mockk
import io.primer.android.payments.core.create.data.datasource.LocalPaymentDataSource
import io.primer.android.payments.core.create.data.model.PaymentDataResponse
import io.primer.android.payments.core.create.data.model.PaymentStatus
import io.primer.android.payments.core.create.data.model.toPaymentResult
import io.primer.android.payments.core.create.domain.repository.PaymentResultRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PaymentResultDataRepositoryTest {

    private lateinit var localPaymentDataSource: LocalPaymentDataSource
    private lateinit var repository: PaymentResultRepository

    @BeforeEach
    fun setUp() {
        localPaymentDataSource = mockk()
        repository = PaymentResultDataRepository(localPaymentDataSource)
    }

    @Test
    fun `getPaymentResult should return expected PaymentResult`() {
        // Arrange
        val paymentDataResponse = PaymentDataResponse(
            id = "payment123",
            date = "2024-06-25",
            status = PaymentStatus.SUCCESS,
            orderId = "order456",
            currencyCode = "USD",
            amount = 1000,
            customerId = "customer789",
            paymentFailureReason = null,
            requiredAction = null,
            showSuccessCheckoutOnPendingPayment = false
        )
        val expectedPaymentResult = paymentDataResponse.toPaymentResult()

        every { localPaymentDataSource.get() } returns paymentDataResponse

        // Act
        val result = repository.getPaymentResult()

        // Assert
        assertEquals(expectedPaymentResult, result)
    }
}
