package io.primer.android.payments.core.create.data.datasource

import io.mockk.mockk
import io.primer.android.payments.core.create.data.model.PaymentDataResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class LocalPaymentDataSourceTest {

    private lateinit var localPaymentDataSource: LocalPaymentDataSource

    @BeforeEach
    fun setup() {
        localPaymentDataSource = LocalPaymentDataSource()
    }

    @Test
    fun `test update and get`() {
        // Arrange
        val paymentDataResponse = mockk<PaymentDataResponse>()

        // Act
        localPaymentDataSource.update(paymentDataResponse)
        val result = localPaymentDataSource.get()

        // Assert
        assertEquals(paymentDataResponse, result)
    }

    @Test
    fun `test get without update throws UninitializedPropertyAccessException`() {
        // Act & Assert
        val exception = assertThrows<UninitializedPropertyAccessException> {
            localPaymentDataSource.get()
        }

        assertEquals("lateinit property paymentResponse has not been initialized", exception.message)
    }
}
